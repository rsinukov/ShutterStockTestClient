package com.rsinukov.shutterstockclient.search.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import com.rsinukov.shutterstockclient.BaseFragment
import com.rsinukov.shutterstockclient.R
import com.rsinukov.shutterstockclient.dagger.ShutterStockDagger
import com.rsinukov.shutterstockclient.search.SearchComponent
import com.rsinukov.shutterstockclient.search.bl.Intention
import com.rsinukov.shutterstockclient.search.bl.SearchState
import com.rsinukov.shutterstockclient.utils.ui.bindView
import com.rsinukov.shutterstockclient.utils.ui.visible
import com.veon.common.rx.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SearchFragment : BaseFragment() {

    private companion object {
        const val LOAD_MORE_TRESHOLD = 10
        const val QUERY_DEBOUNCE_MS = 400L
    }

    private val recyclerView: RecyclerView by bindView(R.id.fragment_search_recycler)
    private val searchView: SearchView by bindView(R.id.fragment_search_search_view)
    private val toolbar: Toolbar by bindView(R.id.fragment_search_toolbar)

    private val disposables = CompositeDisposable()
    private val intentionsSubject = PublishSubject.create<Intention>()

    private lateinit var adapter: SearchAdapter

    private var hasMoreItems = false
    private var loadingInProgress = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO add pull to refresh with Reload Intention on trigger
        // TODO add loading indicator to search view by using state.loadingInProgress

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        initRecycler()

        val presenter = ShutterStockDagger.getInstance(activity!!)[SearchComponent::class.java].providePresenter()
        disposables += CompositeDisposable(
            presenter.states()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { render(it) },
            presenter.processIntentions(intentions())
        )
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    override fun onScopeFinished() {
        ShutterStockDagger.getInstance(activity!!).remove(SearchComponent::class.java)
        super.onScopeFinished()
    }

    private fun intentions(): Observable<out Intention> {
        return Observable.merge(
            Observable.just(Intention.Initial),
            intentionsSubject,
            searchViewObservable()
        )
    }

    private fun render(state: SearchState) {
        with(state) {
            hasMoreItems = hasMore

            recyclerView.visible = true
            val moreState = when {
                state.isError -> SearchAdapter.MoreState.Error
                state.hasMore -> SearchAdapter.MoreState.HasMore
                else -> SearchAdapter.MoreState.Full
            }

            adapter.setItems(content, moreState)
        }

        if (loadingInProgress) {
            loadingInProgress = state.isLoading
        }
    }

    private fun initRecycler() {
        recyclerView.visible = false
        adapter = SearchAdapter(activity!!) {
            intentionsSubject.onNext(Intention.TryAgain(searchView.query.toString()))
        }
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(activity!!)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searchView.hasFocus()) {
                    searchView.clearFocus()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (!hasMoreItems || loadingInProgress) return
                if (layoutManager.findLastVisibleItemPosition() + LOAD_MORE_TRESHOLD > adapter.itemCount) {
                    loadingInProgress = true
                    intentionsSubject.onNext(Intention.LoadMore(searchView.query.toString()))
                }
            }
        })
    }

    private fun searchViewObservable(): Observable<Intention> {
        return RxSearchView.queryTextChanges(searchView)
            .debounce(QUERY_DEBOUNCE_MS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .map { it.toString() }
            .distinctUntilChanged()
            .map { Intention.Load(it) }
    }
}
