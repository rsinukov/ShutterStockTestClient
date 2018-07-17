package com.rsinukov.shutterstockclient.features.templateslist.ui

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
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

class SearchFragment : BaseFragment() {

    private val okButton: View by bindView(R.id.templates_list_ok)
    private val cancelButton: View by bindView(R.id.templates_list_cancel)
    private val viewPager: ViewPager by bindView(R.id.templates_list_pager)
    private val contentView: View by bindView(R.id.templates_list_content_group)
    private val errorView: View by bindView(R.id.templates_list_error)
    private val loadingView: View by bindView(R.id.templates_list_loading)

    private val argbEvaluator = ArgbEvaluator()

    private val disposables = CompositeDisposable()
    private val intentionsSubject = PublishSubject.create<Intention>()

    private lateinit var templatesListAdapter: TemplatesListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentView.visible = false
        errorView.visible = false
        loadingView.visible = true

        val presenter = ShutterStockDagger.getInstance(activity!!)[SearchComponent::class.java].providePresenter()

        disposables += CompositeDisposable(
            presenter.states()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { render(it) },
            presenter.processIntentions(intentions())
        )

        initViewPager()

        errorView.setOnClickListener { intentionsSubject.onNext(Intention.Reload) }
        okButton.setOnClickListener { Toast.makeText(activity, "Go to next screen", Toast.LENGTH_SHORT).show() }
        cancelButton.setOnClickListener { Toast.makeText(activity, "Go to prev screen", Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    override fun onScopeFinished() {
        ShutterStockDagger.getInstance(activity!!).remove(SearchComponent::class.java)
        super.onScopeFinished()
    }

    private fun initViewPager() {
        templatesListAdapter = TemplatesListAdapter(
            activity!!,
            { templateId, variantId ->
                intentionsSubject.onNext(Intention.TemplateVariationSelected(templateId, variantId))
            }
        )
        viewPager.adapter = templatesListAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val nextPosition = if (positionOffset > 0) position + 1 else position - 1
                if (nextPosition < 0 || nextPosition >= templatesListAdapter.items.size) return

                val currentTemplate = templatesListAdapter.items[position]
                val nextTemplate = templatesListAdapter.items[nextPosition]

                viewPager.setBackgroundColor(
                    argbEvaluator.evaluate(Math.abs(positionOffset), currentTemplate.color, nextTemplate.color) as Int
                )
            }

            override fun onPageSelected(position: Int) = Unit

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    val templateId = templatesListAdapter.items[viewPager.currentItem].id
                    intentionsSubject.onNext(Intention.TemplateSelected(templateId))
                }
            }
        })

        val localView = view ?: return
        localView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val horizontalPadding = localView.width / 10
                viewPager.setPadding(horizontalPadding, viewPager.paddingTop, horizontalPadding, viewPager.right)
                localView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun intentions(): Observable<out Intention> {
        return Observable.merge(
            Observable.just(Intention.Initial),
            intentionsSubject
        )
    }

    private fun render(state: SearchState) {
        with(state) {
            loadingView.visible = isLoading
            errorView.visible = isError

            contentView.visible = !isLoading && !isError
            if (content.isNotEmpty()) {
                templatesListAdapter.items = content
                viewPager.setCurrentItem(selectedTemplateIndex, false)
                intentionsSubject.onNext(Intention.TemplateSelected(content[selectedTemplateIndex].id))
                viewPager.setBackgroundColor(content[selectedTemplateIndex].color)
            }
        }
    }
}
