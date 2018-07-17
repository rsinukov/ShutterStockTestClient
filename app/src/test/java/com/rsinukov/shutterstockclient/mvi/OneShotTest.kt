package com.rsinukov.shutterstockclient.mvi

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OneShotTest {

    @Test
    fun `when use empty object should always return null value`() {
        val empty = OneShot.empty<Boolean>()
        val notEmpty = OneShot(true)
        val viewStateWithId = ViewStateWithId()
        assertThat(empty.get(viewStateWithId)).isNull()
        assertThat(notEmpty.get(viewStateWithId)).isNotNull()
    }

    @Test
    fun `when get method called it should remember view state id and return result only on that id`() {
        val oneShot = OneShot(true)
        val viewStateWithIdFirst = ViewStateWithId()
        val viewStateViewIdLast = ViewStateWithId()
        assertThat(oneShot.get(viewStateWithIdFirst)).isTrue()
        assertThat(oneShot.get(viewStateViewIdLast)).isNull()
        assertThat(oneShot.get(viewStateWithIdFirst)).isTrue()
    }
}
