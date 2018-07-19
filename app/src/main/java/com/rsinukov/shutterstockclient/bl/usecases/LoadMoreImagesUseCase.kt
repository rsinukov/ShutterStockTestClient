package com.rsinukov.shutterstockclient.bl.usecases

import com.rsinukov.shutterstockclient.bl.DEFAULT_PAGE_SIZE
import com.rsinukov.shutterstockclient.bl.network.ShutterStockSearchApi
import com.rsinukov.shutterstockclient.bl.network.hasMore
import com.rsinukov.shutterstockclient.bl.storage.Image
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import io.reactivex.Single
import java.net.URLEncoder
import javax.inject.Inject

class LoadMoreImagesUseCase @Inject constructor(
    private val shutterStockSearchApi: ShutterStockSearchApi,
    private val searchRepository: SearchRepository
) {

    fun execute(query: String, page: Int, pageSize: Int = DEFAULT_PAGE_SIZE): Single<Result> {
        return shutterStockSearchApi.search(URLEncoder.encode(query, "UTF-8"), page = page, perPage = pageSize)
            .flatMap { response ->
                val images = response.data.map { it.toEntity() }
                searchRepository.insertImages(query, images)
                    .toSingleDefault(Result(images, response.hasMore()))
            }
    }

    data class Result(
        val images: List<Image>,
        val hasMore: Boolean
    )
}

