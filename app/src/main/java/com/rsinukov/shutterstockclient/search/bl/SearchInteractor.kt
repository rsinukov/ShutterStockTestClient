package com.rsinukov.shutterstockclient.search.bl

import com.rsinukov.shutterstockclient.bl.DEFAULT_PAGE_SIZE
import com.rsinukov.shutterstockclient.bl.START_PAGE
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import com.rsinukov.shutterstockclient.bl.usecases.LoadMoreImagesUseCase
import com.rsinukov.shutterstockclient.bl.usecases.RefreshImagesUseCase
import com.rsinukov.shutterstockclient.dagger.IoScheduler
import com.rsinukov.shutterstockclient.features.templateslist.SearchScope
import com.rsinukov.shutterstockclient.mvi.MVIInteractor
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@SearchScope
class SearchInteractor @Inject constructor(
    @IoScheduler private val scheduler: Scheduler,
    private val loadImagesUseCase: LoadMoreImagesUseCase,
    private val refreshImagesUseCase: RefreshImagesUseCase,
    private val searchRepository: SearchRepository
) : MVIInteractor<Action, Result> {

    private val currentPages = ConcurrentHashMap<String, Int>().withDefault { START_PAGE }

    private val loadProcessor: ObservableTransformer<Action.Load, Result> =
        ObservableTransformer { action ->
            action.switchMap {
                searchRepository.observeImages(it.query, currentPages.getValue(it.query) * DEFAULT_PAGE_SIZE)
                    .map { Result.ImagesLoaded(it) }
            }
        }

    private val loadMoreProcessor: ObservableTransformer<Action.LoadMore, Result> =
        ObservableTransformer { action ->
            action.flatMapSingle {
                loadImagesUseCase.execute(it.query, currentPages.getValue(it.query))
                    .map { Result.HasMoreUpdated(it) }
            }
        }

    private val refreshProcessor: ObservableTransformer<Action.Refresh, Result> =
        ObservableTransformer { action ->
            action.flatMapSingle {
                currentPages[it.query] = START_PAGE
                refreshImagesUseCase.execute(it.query)
                    .map { Result.HasMoreUpdated(it) }
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
}