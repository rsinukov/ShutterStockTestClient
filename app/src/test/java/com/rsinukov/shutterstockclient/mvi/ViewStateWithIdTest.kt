package com.rsinukov.shutterstockclient.mvi

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class ViewStateWithIdTest {

    @Suppress("UNCHECKED_CAST")
    @Before
    fun setUp() {
        ViewStateWithId.Companion::class.memberProperties
                .first { it.name == "lastViewStateId" }
                .let {
                    val prop = (it as KProperty<AtomicLong>).apply { isAccessible = true }
                    prop.getter.call(ViewStateWithId.Companion).set(1)
                }
    }

    @Test
    fun `first created view state with id should has view state id equal to one`() {
        val viewStateWithId = ViewStateWithId()
        assertThat(viewStateWithId.viewStateId).isEqualTo(1L)
    }

    @Test
    fun `each time when view state with id created it should has incremented view state id`() {
        val viewStateWithIdOne = ViewStateWithId()
        val viewStateWithIdTwo = ViewStateWithId()
        val viewStateWithIdThree = ViewStateWithId()
        assertThat(viewStateWithIdOne.viewStateId).isEqualTo(1L)
        assertThat(viewStateWithIdTwo.viewStateId).isEqualTo(2L)
        assertThat(viewStateWithIdThree.viewStateId).isEqualTo(3L)
    }
}
