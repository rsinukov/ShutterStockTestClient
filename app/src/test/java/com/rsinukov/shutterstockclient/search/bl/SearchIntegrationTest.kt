package com.rsinukov.shutterstockclient.search.bl

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.rsinukov.shutterstockclient.bl.network.Assets
import com.rsinukov.shutterstockclient.bl.network.Data
import com.rsinukov.shutterstockclient.bl.network.SearchResponse
import com.rsinukov.shutterstockclient.bl.network.ShutterStockSearchApi
import com.rsinukov.shutterstockclient.bl.storage.Asset
import com.rsinukov.shutterstockclient.bl.storage.Image
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import com.rsinukov.shutterstockclient.bl.usecases.LoadMoreImagesUseCase
import com.rsinukov.shutterstockclient.bl.usecases.RefreshImagesUseCase
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import com.rsinukov.shutterstockclient.bl.network.Asset as NetworkAsset

class SearchIntegrationTest {

    @Mock
    lateinit var shutterStockSearchApi: ShutterStockSearchApi

    private lateinit var searchRepository: SearchRepository
    private lateinit var searchPresenter: SearchPresenter
    private lateinit var searchInteractor: SearchInteractor
    private lateinit var loadImagesUseCase: LoadMoreImagesUseCase
    private lateinit var refreshImagesUseCase: RefreshImagesUseCase

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        searchRepository = SearchRepository(scheduler)
        loadImagesUseCase = LoadMoreImagesUseCase(shutterStockSearchApi, searchRepository)
        refreshImagesUseCase = RefreshImagesUseCase(shutterStockSearchApi, searchRepository)
        searchInteractor = SearchInteractor(scheduler, loadImagesUseCase, refreshImagesUseCase, searchRepository)
        searchPresenter = SearchPresenter(searchInteractor)
    }

    @Test
    fun `on initial intention should produce no state`() {
        val states = searchPresenter.states().test()
        searchPresenter.processIntentions(Observable.just(Intention.Initial))
        states.assertNoValues()
    }

    @Test
    fun `on load intention should show data from repo`() {
        searchRepository.insertImages("test", mappedImages()).blockingAwait()

        val states = searchPresenter.states().test()
        searchPresenter.processIntentions(Observable.just(Intention.Initial, Intention.Load("test")))

        states.assertValue(
            SearchState(
                isLoading = false,
                isError = false,
                hasMore = true,
                content = viewModels()
            )
        )
    }

    @Test
    fun `on load intention should load and show data with loading states if not in repo and was not loaded before`() {
        whenever(shutterStockSearchApi.search(eq("test"), eq(1), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 1,
                perPage = 30,
                totalCount = 50,
                data = responseData()
            )
        ))

        val states = searchPresenter.states().test()
        searchPresenter.processIntentions(Observable.just(Intention.Initial, Intention.Load("test")))

        states.assertValues(
            // loading started
            SearchState(
                isLoading = true,
                isError = false,
                hasMore = true,
                content = emptyList()
            ),
            // has more updated
            SearchState(
                isLoading = true,
                isError = false,
                hasMore = true,
                content = emptyList()
            ),
            // has more finished
            SearchState(
                isLoading = false,
                isError = false,
                hasMore = true,
                content = emptyList()
            ),
            // data updated
            SearchState(
                isLoading = false,
                isError = false,
                hasMore = true,
                content = viewModels()
            )
        )
    }

    @Test
    fun `on load intention should show empty data if not in repo and was loaded before`() {
        whenever(shutterStockSearchApi.search(eq("test"), eq(1), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 1,
                perPage = 30,
                totalCount = 50,
                data = emptyList()
            )
        ))

        val states = searchPresenter.states().test()
        searchPresenter.processIntentions(Observable.just(Intention.Initial, Intention.Load("test")))
        searchPresenter.processIntentions(Observable.just(Intention.Initial, Intention.Load("test")))

        states.assertValueAt(
            states.valueCount() - 1,
            SearchState(
                isLoading = false,
                isError = false,
                hasMore = false,
                content = emptyList()
            )
        )
        verify(shutterStockSearchApi, times(1)).search(eq("test"), any(), any(), any())
    }

    @Test
    fun `on loadMore intention should load more and show more from repo with hasMore=true if not all images was loaded`() {
        whenever(shutterStockSearchApi.search(eq("test"), eq(1), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 1,
                perPage = 30,
                totalCount = 80,
                data = responseData(30)
            )
        ))

        val states = searchPresenter.states().test()
        val intentions = PublishSubject.create<Intention>()
        searchPresenter.processIntentions(intentions)
        intentions.onNext(Intention.Initial)
        intentions.onNext(Intention.Load("test"))

        whenever(shutterStockSearchApi.search(eq("test"), eq(2), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 2,
                perPage = 30,
                totalCount = 80,
                data = responseData(60).subList(30, 60)
            )
        ))

        intentions.onNext(Intention.LoadMore("test"))

        assertThat(states.values()).contains(
            SearchState(
                isLoading = false,
                isError = false,
                hasMore = true,
                content = viewModels(60)
            )
        )
    }

    @Test
    fun `on loadMore intention should update hasMore=false if all images loaded`() {
        whenever(shutterStockSearchApi.search(eq("test"), eq(1), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 1,
                perPage = 30,
                totalCount = 50,
                data = responseData(30)
            )
        ))

        val states = searchPresenter.states().test()
        val intentions = PublishSubject.create<Intention>()
        searchPresenter.processIntentions(intentions)
        intentions.onNext(Intention.Initial)
        intentions.onNext(Intention.Load("test"))

        whenever(shutterStockSearchApi.search(eq("test"), eq(2), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 2,
                perPage = 30,
                totalCount = 50,
                data = responseData(50).subList(30, 50)
            )
        ))

        intentions.onNext(Intention.LoadMore("test"))

        assertThat(states.values()).contains(
            SearchState(
                isLoading = false,
                isError = false,
                hasMore = false,
                content = viewModels(50)
            )
        )
    }

    @Test
    fun `on loadMore intention should show error if request failed`() {
        whenever(shutterStockSearchApi.search(eq("test"), eq(1), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 1,
                perPage = 30,
                totalCount = 50,
                data = responseData(30)
            )
        ))

        val states = searchPresenter.states().test()
        val intentions = PublishSubject.create<Intention>()
        searchPresenter.processIntentions(intentions)
        intentions.onNext(Intention.Initial)
        intentions.onNext(Intention.Load("test"))

        whenever(shutterStockSearchApi.search(eq("test"), eq(2), any(), any())).thenReturn(Single.error(RuntimeException("failed")))

        intentions.onNext(Intention.LoadMore("test"))

        assertThat(states.values()).contains(
            SearchState(
                isLoading = false,
                isError = true,
                hasMore = true,
                content = viewModels(30)
            )
        )
    }

    // todo more tests:
    // only success loadMore incs page
    // pages changed independently for different queries
    // refresh sets page to 1
    // refresh triggers loading/error/hasMore state changes
    // loadMore triggers loading/error/hasMore state changes

    private fun responseData(count: Int = 2): List<Data> {
        val data = mutableListOf<Data>()
        repeat(count) { id ->
            data.add(
                Data(
                    aspect = 1.0f,
                    description = "title$id",
                    id = "id$id",
                    assets = Assets(NetworkAsset(100, 100, "testUrl$id"))
                )
            )
        }
        return data
    }


    private fun mappedImages(count: Int = 2): List<Image> {
        val data = mutableListOf<Image>()
        repeat(count) { id ->
            data.add(
                Image(
                    aspect = 1.0f,
                    description = "title$id",
                    id = "id$id",
                    preview = Asset(100, 100, "testUrl$id")
                )
            )
        }
        return data
    }

    private fun viewModels(count: Int = 2): List<ImageViewModel> {
        val data = mutableListOf<ImageViewModel>()
        repeat(count) { id ->
            data.add(
                ImageViewModel(
                    aspect = 1.0f,
                    description = "title$id",
                    id = "id$id",
                    previewUrl = "testUrl$id"
                )
            )
        }
        return data
    }
}
