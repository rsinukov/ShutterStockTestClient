package com.rsinukov.shutterstockclient.bl.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.rsinukov.shutterstockclient.bl.network.Template as TemplateResponse
import com.rsinukov.shutterstockclient.bl.network.ShutterStockSearchApi
import com.rsinukov.shutterstockclient.bl.storage.Template
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LoadImagesUseCaseTest {

    @Mock
    lateinit var shutterStockSearchApi: ShutterStockSearchApi
    @Mock
    lateinit var searchRepository: SearchRepository

    lateinit var loadImagesUseCase: LoadImagesUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        loadImagesUseCase = spy(LoadImagesUseCase(shutterStockSearchApi, searchRepository))
    }

    @Test
    fun `execute should load templates and put them to repository`() {
        whenever(shutterStockSearchApi.getTemplates()).thenReturn(Single.just(listOf("template1", "template2")))

        val template1 = mock<TemplateResponse>()
        whenever(shutterStockSearchApi.getTemplate("template1")).thenReturn(Single.just(template1))
        val template2 = mock<TemplateResponse>()
        whenever(shutterStockSearchApi.getTemplate("template2")).thenReturn(Single.just(template2))

        val templateEntity1 = mock<Template>()
        doReturn(templateEntity1).whenever(loadImagesUseCase).toStorageEntity(template1)
        val templateEntity2 = mock<Template>()
        doReturn(templateEntity2).whenever(loadImagesUseCase).toStorageEntity(template2)

        whenever(searchRepository.saveTemplates(any())).thenReturn(Completable.complete())

        loadImagesUseCase.execute().test().assertComplete()

        verify(searchRepository).saveTemplates(listOf(templateEntity1, templateEntity2))
    }
}
