package com.rsinukov.shutterstockclient.mvi

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MVIBasePresenterTest {

    @Mock
    private lateinit var interactor: MVIInteractor<Action, Result>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(interactor.actionProcessor())
                .thenReturn(ObservableTransformer { actions ->
                    actions.flatMap { _ -> Observable.just(Result) }
                })
    }

    @Test
    fun `when states subscribed should return no state`() {
        val presenter = TestPresenter(interactor)

        presenter.states()
                .test()
                .assertNoValues()
    }

    @Test
    fun `when resubscribe to same presenter should change initial intention to last state intention`() {
        val mapper = Function<Action, Observable<Result>> {
            Observable.fromCallable { Result }
        }
        val spyMapper = spy(mapper)
        val transformer = ObservableTransformer<Action, Result> { actions ->
            actions.flatMap(spyMapper)
        }

        whenever(interactor.actionProcessor()).thenReturn(transformer)

        val presenter = TestPresenter(interactor)
        val intentionsSubject = PublishSubject.create<Intention>()
        presenter.states().subscribe()

        presenter.processIntentions(intentionsSubject)
        intentionsSubject.onNext(TestInitialIntention)
        verify(spyMapper).apply(InitialAction)

        presenter.states().subscribe()
        intentionsSubject.onNext(TestInitialIntention)
        verify(spyMapper).apply(LastStateAction)
    }

    @Test
    fun `when presenter is destroyed no new states should be generated`() {
        val presenter = TestPresenter(interactor)
        val intentionsSubject = PublishSubject.create<Intention>()
        val testObserver = presenter.states().test()

        presenter.processIntentions(intentionsSubject)

        intentionsSubject.onNext(LastStateIntention)

        presenter.destroy()

        intentionsSubject.onNext(LastStateIntention)
        intentionsSubject.onNext(LastStateIntention)

        testObserver.assertValueCount(1)
    }

    @Test
    fun `when presenter is destroyed inner circle must be unsubscribed`() {
        val mapper = Function<Action, Observable<Result>> {
            Observable.fromCallable { Result }
        }
        val spyMapper = spy(mapper)
        val transformer = ObservableTransformer<Action, Result> { actions ->
            actions.flatMap(spyMapper)
        }

        whenever(interactor.actionProcessor()).thenReturn(transformer)

        val presenter = TestPresenter(interactor)
        val intentionsSubject = PublishSubject.create<Intention>()
        presenter.states().subscribe()

        presenter.processIntentions(intentionsSubject)

        presenter.destroy()

        intentionsSubject.onNext(LastStateIntention)
        intentionsSubject.onNext(LastStateIntention)

        verify(spyMapper, never()).apply(any())
    }

    class TestPresenter(interactor: MVIInteractor<Action, Result>) : MVIBasePresenter<Intention, State, Action, Result>(interactor) {

        override val defaultState: State
            get() = State

        override val lastStateIntention: LastStateIntention
            get() = LastStateIntention

        override fun intentionToActionMapper(): (Intention) -> Action {
            return {
                when (it) {
                    TestInitialIntention -> InitialAction
                    LastStateIntention -> LastStateAction
                }
            }
        }

        override fun stateReducer(): (State, Result) -> State {
            return { state, _ -> state }
        }
    }
}

sealed class Intention
object TestInitialIntention : Intention(), com.rsinukov.shutterstockclient.mvi.InitialIntention
object LastStateIntention : Intention()

sealed class Action
object InitialAction : Action()
object LastStateAction : Action()

object State
object Result
