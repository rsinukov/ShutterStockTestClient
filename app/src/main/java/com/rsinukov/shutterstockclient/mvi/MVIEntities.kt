package com.rsinukov.shutterstockclient.mvi

import com.veon.common.rx.toDisposableObserver
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

interface MVIPresenter<in Intention, State> {
    fun processIntentions(intentions: Observable<out Intention>): Disposable
    fun states(): Observable<State>
    fun destroy()
}

abstract class MVIBasePresenter<Intention, State, Action, Result>(
    private val interactor: MVIInteractor<Action, Result>
) : MVIPresenter<Intention, State> {

    private val intentionsSubject = PublishSubject.create<Intention>()
    private val stateSubject = PublishSubject.create<State>()
    private val disposables: CompositeDisposable = CompositeDisposable()

    init {
        disposables.add(
            compose().subscribeWith(stateSubject.toDisposableObserver())
        )
    }

    private fun initialIntentionFilter() =
        { _: Intention, newIntention: Intention ->
            when (newIntention) {
                is InitialIntention -> lastStateIntention
                else -> newIntention
            }
        }

    override fun processIntentions(intentions: Observable<out Intention>): Disposable {
        return intentions.subscribeWith(intentionsSubject.toDisposableObserver())
    }

    override fun states(): Observable<State> {
        return stateSubject
    }

    override fun destroy() {
        disposables.dispose()
    }

    private fun compose(): Observable<State> {
        return intentionsSubject
            .scan(initialIntentionFilter())
            .map(intentionToActionMapper())
            .compose(interactor.actionProcessor())
            .scan(defaultState, stateReducer())
    }

    /**
     * Override via explicit get, ex:
     * ```
     *   override val defaultState
     *      get() = SomeState()
     * ```
     */
    protected abstract val defaultState: State

    protected abstract val lastStateIntention: Intention

    protected abstract fun intentionToActionMapper(): (Intention) -> Action

    protected abstract fun stateReducer(): (State, Result) -> State
}

interface MVIInteractor<in Action, out Result> {
    fun actionProcessor(): ObservableTransformer<in Action, out Result>
}

interface InitialIntention
