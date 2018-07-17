package com.rsinukov.shutterstockclient.search.bl

import com.rsinukov.shutterstockclient.bl.storage.Image
import com.rsinukov.shutterstockclient.search.SearchScope
import com.rsinukov.shutterstockclient.mvi.MVIBasePresenter
import javax.inject.Inject

@SearchScope
class SearchPresenter @Inject constructor(
    interactor: SearchInteractor
) : MVIBasePresenter<Intention, SearchState, Action, Result>(interactor) {

    override val defaultState: SearchState
        get() = SearchState(
            isLoading = false,
            isError = false,
            content = emptyList(),
            hasMore = true
        )

    override val lastStateIntention: Intention
        get() = Intention.LastState

    override fun intentionToActionMapper(): (Intention) -> Action = {
        when (it) {
            Intention.Initial -> Action.Initial
            Intention.LastState -> Action.LastState
            is Intention.TryAgain -> Action.LoadMore(it.query)
            is Intention.Refresh -> Action.Refresh(it.query)
            is Intention.Load -> Action.Load(it.query)
            is Intention.LoadMore -> Action.LoadMore(it.query)
        }
    }

    override fun stateReducer(): (SearchState, Result) -> SearchState = { prevState, result ->
        when (result) {
            Result.LoadingError -> prevState.copy(isError = true, isLoading = false)
            Result.LoadingStarted -> prevState.copy(isError = false, isLoading = true)
            Result.LoadingFinished -> prevState.copy(isError = false, isLoading = false)
            Result.LastState -> prevState
            is Result.ImagesLoaded -> prevState.copy(content = result.images.map { it.toViewModel() })
            is Result.HasMoreUpdated -> prevState.copy(hasMore = result.hasMore)
        }
    }

    private fun Image.toViewModel(): ImageViewModel {
        return ImageViewModel(
            id = id,
            aspect = preview.width.toFloat() / preview.height.toFloat(),
            description = description,
            previewUrl = preview.url
        )
    }
}
