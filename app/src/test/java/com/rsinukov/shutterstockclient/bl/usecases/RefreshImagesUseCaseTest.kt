package com.rsinukov.shutterstockclient.bl.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.rsinukov.shutterstockclient.bl.DEFAULT_PAGE_SIZE
import com.rsinukov.shutterstockclient.bl.network.Assets
import com.rsinukov.shutterstockclient.bl.network.Data
import com.rsinukov.shutterstockclient.bl.network.PARAM_FIELDS
import com.rsinukov.shutterstockclient.bl.network.SearchResponse
import com.rsinukov.shutterstockclient.bl.network.ShutterStockSearchApi
import com.rsinukov.shutterstockclient.bl.storage.Asset
import com.rsinukov.shutterstockclient.bl.storage.Image
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.net.URLEncoder
import com.rsinukov.shutterstockclient.bl.network.Asset as NetworkAsset

class RefreshImagesUseCaseTest {

    @Mock
    lateinit var shutterStockSearchApi: ShutterStockSearchApi
    @Mock
    lateinit var searchRepository: SearchRepository

    lateinit var refreshImagesUseCase: RefreshImagesUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        refreshImagesUseCase = RefreshImagesUseCase(shutterStockSearchApi, searchRepository)
    }

    @Test
    fun `execute should call api with url encoded query`() {
        whenever(shutterStockSearchApi.search(any(), any(), any(), any())).thenReturn(Single.never())

        refreshImagesUseCase.execute("test query").test()
        verify(shutterStockSearchApi).search(
            query = URLEncoder.encode("test query", "UTF-8"),
            page = 1,
            perPage = DEFAULT_PAGE_SIZE,
            fields = PARAM_FIELDS
        )
    }

    @Test
    fun `execute should save response to repository`() {
        whenever(shutterStockSearchApi.search(any(), any(), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 1,
                perPage = 1,
                totalCount = 2,
                data = responseData()
            )
        ))

        refreshImagesUseCase.execute("test query").test()

        verify(searchRepository).clearAndInsertImages("test query", mappedImages())
    }

    @Test
    fun `execute should return has more true if total count more then loaded`() {
        whenever(shutterStockSearchApi.search(any(), any(), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 1,
                perPage = 1,
                totalCount = 2,
                data = responseData()
            )
        ))
        whenever(searchRepository.clearAndInsertImages(any(), any())).thenReturn(Completable.complete())

        val test = refreshImagesUseCase.execute("test query").test()

        test.assertValue(true)
    }

    @Test
    fun `execute should return images and has more false if total count more or equal then loaded`() {
        whenever(shutterStockSearchApi.search(any(), any(), any(), any())).thenReturn(Single.just(
            SearchResponse(
                page = 2,
                perPage = 2,
                totalCount = 4,
                data = responseData()
            )
        ))
        whenever(searchRepository.clearAndInsertImages(any(), any())).thenReturn(Completable.complete())

        val test = refreshImagesUseCase.execute("test query").test()

        test.assertValue(false)
    }

    private fun responseData(): List<Data> {
        return listOf(
            Data(
                aspect = 1.0f,
                description = "title1",
                id = "id1",
                assets = Assets(NetworkAsset(100, 100, "testUrl1"))
            ),
            Data(
                aspect = 2.0f,
                description = "title2",
                id = "id2",
                assets = Assets(NetworkAsset(200, 200, "testUrl2"))
            )
        )
    }

    private fun mappedImages(): List<Image> {
        return listOf(
            Image(
                aspect = 1.0f,
                description = "title1",
                id = "id1",
                preview = Asset(100, 100, "testUrl1")
            ),
            Image(
                aspect = 2.0f,
                description = "title2",
                id = "id2",
                preview = Asset(200, 200, "testUrl2")
            )
        )
    }
}
