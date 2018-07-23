package com.rsinukov.shutterstockclient.search.bl

import com.rsinukov.shutterstockclient.bl.storage.Image
import com.rsinukov.shutterstockclient.mvi.InitialIntention

data class SearchState(
    val isLoading: Boolean,
    val isError: Boolean,
    val content: List<ImageViewModel>,
    val hasMore: Boolean
)

sealed class Intention {
    object Initial : Intention(), InitialIntention
    object LastState : Intention()
    data class TryAgain(val query: String) : Intention()
    data class Refresh(val query: String) : Intention()
    data class Load(val query: String) : Intention()
    data class LoadMore(val query: String) : Intention()
}

sealed class Action {
    object Initial : Action()
    object LastState : Action()
    data class Load(val query: String) : Action()
    data class Refresh(val query: String) : Action()
    data class LoadMore(val query: String) : Action()
}

sealed class Result {
    object LastState : Result()
    object LoadingStarted : Result()
    object LoadingFinished : Result()
    object LoadingError : Result()
    data class ImagesLoaded(val images: List<Image>) : Result()
    data class HasMoreUpdated(val hasMore: Boolean) : Result()
}
