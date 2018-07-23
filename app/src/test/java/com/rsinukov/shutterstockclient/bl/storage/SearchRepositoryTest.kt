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
    fun `observeImages should be empty by default`() {
        searchRepository.observeImages("test", 100).test()
            .assertValue(emptyList())
            .assertNotTerminated()
    }

    @Test
    fun `insertImages should add to exisiting`() {
        val test = searchRepository.observeImages("test", 100).test()

        val list1 = listOf<Image>(mock(), mock())
        searchRepository.insertImages("test", list1).blockingAwait()
        test.assertValues(emptyList(), list1)

        val list2 = listOf<Image>(mock(), mock())
        searchRepository.insertImages("test", list2).blockingAwait()
        test.assertValues(emptyList(), list1, list1 + list2)
    }

    @Test
    fun `insertImages should save only for provided query`() {
        val test1 = searchRepository.observeImages("test1", 100).distinctUntilChanged().test()
        val test2 = searchRepository.observeImages("test2", 100).distinctUntilChanged().test()

        val list1 = listOf<Image>(mock(), mock())
        val list2 = listOf<Image>(mock(), mock())

        searchRepository.insertImages("test1", list1).blockingAwait()
        searchRepository.insertImages("test2", list2).blockingAwait()

        test1.assertValues(emptyList(), list1)
        test2.assertValues(emptyList(), list2)
    }

    @Test
    fun `clearAndInsertImages should reset query`() {
        val test = searchRepository.observeImages("test", 100).distinctUntilChanged().test()

        val list1 = listOf<Image>(mock(), mock())
        searchRepository.insertImages("test", list1).blockingAwait()
        test.assertValues(emptyList(), list1)

        val list2 = listOf<Image>(mock(), mock())
        searchRepository.clearAndInsertImages("test", list2).blockingAwait()
        test.assertValues(emptyList(), list1, list2)
    }

    @Test
    fun `clearAndInsertImages should reset query only for provided query`() {
        val test1 = searchRepository.observeImages("test1", 100).distinctUntilChanged().test()
        val test2 = searchRepository.observeImages("test2", 100).distinctUntilChanged().test()

        val list1 = listOf<Image>(mock(), mock())
        val list2 = listOf<Image>(mock(), mock())
        searchRepository.clearAndInsertImages("test1", list1).blockingAwait()
        searchRepository.clearAndInsertImages("test2", list2).blockingAwait()

        test1.assertValues(emptyList(), list1)
        test2.assertValues(emptyList(), list2)
    }
}
