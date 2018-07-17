package com.rsinukov.shutterstockclient.bl.storage

import com.nhaarman.mockito_kotlin.mock
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class SearchRepositoryTest {

    lateinit var searchRepository: SearchRepository

    @Before
    fun setUp() {
        searchRepository = SearchRepository(Schedulers.trampoline())
    }

    @Test
    fun `observeTemplates should return empty list by default`() {
        searchRepository.observeTemplates().test()
            .assertValue(emptyList())
            .assertNotTerminated()
    }

    @Test
    fun `when save templates should update value`() {
        val subscriber = searchRepository.observeTemplates().test()
        subscriber.assertValue(emptyList())

        val templates1 = listOf<Template>(mock())
        searchRepository.saveTemplates(templates1).blockingAwait()
        subscriber.assertValues(emptyList(), templates1)

        val templates2 = listOf<Template>(mock(), mock())
        searchRepository.saveTemplates(templates2).blockingAwait()
        subscriber.assertValues(emptyList(), templates1, templates2)
    }
}
