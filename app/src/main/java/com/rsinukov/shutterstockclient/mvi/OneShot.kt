package com.rsinukov.shutterstockclient.mvi

class OneShot<out T>(
    private val value: T?
) {

    companion object {
        private val EMPTY = OneShot(null).apply {
            readInViewStateId = -1L
        }

        fun <T> empty(): OneShot<T> = EMPTY
    }

    private var readInViewStateId: Long? = null

    fun get(viewStateWithId: ViewStateWithId): T? {
        return when (readInViewStateId) {
            viewStateWithId.viewStateId -> value
            null -> {
                readInViewStateId = viewStateWithId.viewStateId
                value
            }
            else -> null
        }
    }

    override fun toString(): String {
        return "OneShot(value=$value, readInViewStateId=$readInViewStateId)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as OneShot<*>

        if (value != other.value) {
            return false
        }
        if (readInViewStateId != other.readInViewStateId) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (readInViewStateId?.hashCode() ?: 0)
        return result
    }
}
