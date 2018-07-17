package com.rsinukov.shutterstockclient.bl.usecases

import com.rsinukov.shutterstockclient.bl.DEFAULT_PAGE_SIZE
import com.rsinukov.shutterstockclient.bl.network.ShutterStockSearchApi
import com.rsinukov.shutterstockclient.bl.network.hasMore
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import io.reactivex.Single
import javax.inject.Inject

class LoadMoreImagesUseCase @Inject constructor(
    private val shutterStockSearchApi: ShutterStockSearchApi,
    private val searchRepository: SearchRepository
) {

    /**
     * Loads next page from api
     * @return if has more images to load
     */
    fun execute(query: String, page: Int, pageSize: Int = DEFAULT_PAGE_SIZE): Single<Boolean> {
        return shutterStockSearchApi.search(query, page = page, perPage = pageSize)
            .flatMap { response ->
                searchRepository.insertImages(query, response.data.map { it.toEntity() })
                    .toSingleDefault(response)
            }
            .map { it.hasMore() }
    }
}

