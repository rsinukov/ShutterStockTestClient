package com.rsinukov.shutterstockclient.mvi

import java.util.concurrent.atomic.AtomicLong

open class ViewStateWithId {

    companion object {

        private val lastViewStateId = AtomicLong(1L)

        fun getNextId() = lastViewStateId.getAndIncrement()
    }

    val viewStateId: Long = getNextId()
}
