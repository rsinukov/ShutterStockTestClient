package com.rsinukov.shutterstockclient.search.bl

import com.rsinukov.shutterstockclient.bl.DEFAULT_PAGE_SIZE
import com.rsinukov.shutterstockclient.bl.START_PAGE
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import com.rsinukov.shutterstockclient.bl.usecases.LoadMoreImagesUseCase
import com.rsinukov.shutterstockclient.bl.usecases.RefreshImagesUseCase
import com.rsinukov.shutterstockclient.dagger.IoScheduler
import com.rsinukov.shutterstockclient.mvi.MVIInteractor
import com.rsinukov.shutterstockclient.search.SearchScope
import com.rsinukov.shutterstockclient.utils.rx.toLoadingStateObservable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@SearchScope
class SearchInteractor @Inject constructor(
    @IoScheduler private val scheduler: Scheduler,
    private val loadImagesUseCase: LoadMoreImagesUseCase,
    private val refreshImagesUseCase: RefreshImagesUseCase,
    private val searchRepository: SearchRepository
) : MVIInteractor<Action, Result> {

    private val loadingInProgress = AtomicBoolean(false)
    private val emptyQueries = CopyOnWriteArraySet<String>()
    private val currentPagesStorage = CurrentPagesStorage()

    private val loadProcessor: ObservableTransformer<Action.Load, Result> =
        ObservableTransformer { actions ->
            actions.switchMap { action ->
                currentPagesStorage.observePage(action.query)
                    .switchMap { page ->
                        searchRepository.observeImages(action.query, page * DEFAULT_PAGE_SIZE)
                            .flatMap { images ->
                                when {
                                // if data in repo -> show it
                                    images.isNotEmpty() -> Observable.just(Result.ImagesLoaded(images))
                                // else if we haven't loaded it before -> load it
                                    !emptyQueries.contains(action.query) -> Single.just(action.query)
                                        .flatMapObservable { query ->
                                            loadingInProgress.set(true)
                                            loadImagesUseCase.execute(query, START_PAGE)
                                                .doOnSuccess { if (it.images.isEmpty()) emptyQueries.add(query) }
                                                .doOnSuccess { currentPagesStorage.incPageSilent(query) }
                                                .map { Result.HasMoreUpdated(it.hasMore) }
                                                .toLoadingObservable()
                                        }
                                // else no data to show -> show empty list
                                    else -> Observable.just(Result.ImagesLoaded(emptyList()))
                                }
                            }
                    }
            }
        }

    private val loadMoreProcessor: ObservableTransformer<Action.LoadMore, Result> =
        ObservableTransformer { actions ->
            actions
                .map { it.query }
                .filter { !loadingInProgress.get() }
                .switchMap { query ->
                    loadingInProgress.set(true)
                    loadImagesUseCase.execute(query, currentPagesStorage.getPage(query))
                        .doOnSuccess { currentPagesStorage.incCurrentPage(query) }
                        .map { Result.HasMoreUpdated(it.hasMore) }
                        .toLoadingObservable()
                }
        }

    private val refreshProcessor: ObservableTransformer<Action.Refresh, Result> =
        ObservableTransformer { actions ->
            actions
                .switchMap {
                    loadingInProgress.set(true)
                    currentPagesStorage.resetCurrentPage(it.query)
                    refreshImagesUseCase.execute(it.query)
                        .map { Result.HasMoreUpdated(it) }
                        .toLoadingObservable()
                }
        }

    override fun actionProcessor(): ObservableTransformer<in Action, out Result> = ObservableTransformer { actions ->
        actions
            .observeOn(scheduler)
            .publish { shared ->
                val observables = listOf(
                    shared.ofType(Action.LastState::class.java)
                        .map { Result.LastState },
                    shared.ofType(Action.Initial::class.java)
                        .ignoreElements()
                        .toObservable<Result>(),
                    shared.ofType(Action.Refresh::class.java)
                        .compose(refreshProcessor),
                    shared.ofType(Action.LoadMore::class.java)
                        .compose(loadMoreProcessor),
                    shared.ofType(Action.Load::class.java)
                        .compose(loadProcessor)
                )
                Observable.merge(observables)
            }
    }

    private fun Single<Result.HasMoreUpdated>.toLoadingObservable(): Observable<Result> {
        return doOnSuccess { loadingInProgress.set(false) }
            .doOnError { loadingInProgress.set(false) }
            .toLoadingStateObservable(
                started = Result.LoadingStarted,
                failed = Result.LoadingError,
                finished = Result.LoadingFinished
            )
    }
}

private class CurrentPagesStorage {

    private val currentPages = BehaviorSubject
        .createDefault(ConcurrentHashMap<String, Int>().withDefault { START_PAGE })
        .toSerialized()

    fun observePage(query: String): Observable<Int> {
        return currentPages.hide()
            .map { it.getValue(query) }
            .distinctUntilChanged()
    }

    fun getPage(query: String): Int {
        val current = currentPages.blockingFirst()
        return current.getValue(query)
    }

    fun resetCurrentPage(query: String) {
        val current = currentPages.blockingFirst()
        current[query] = START_PAGE
        currentPages.onNext(current)
    }

    fun incCurrentPage(query: String) {
        val current = currentPages.blockingFirst()
        current[query] = current.getValue(query) + 1
        currentPages.onNext(current)
    }

    fun incPageSilent(query: String) {
        val current = currentPages.blockingFirst()
        current[query] = current.getValue(query) + 1
    }
}
