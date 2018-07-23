package com.rsinukov.shutterstockclient.bl.usecases

import com.rsinukov.shutterstockclient.bl.DEFAULT_PAGE_SIZE
import com.rsinukov.shutterstockclient.bl.START_PAGE
import com.rsinukov.shutterstockclient.bl.network.ShutterStockSearchApi
import com.rsinukov.shutterstockclient.bl.network.hasMore
import com.rsinukov.shutterstockclient.bl.storage.SearchRepository
import io.reactivex.Single
import java.net.URLEncoder
import javax.inject.Inject

class RefreshImagesUseCase @Inject constructor(
    private val shutterStockSearchApi: ShutterStockSearchApi,
    private val searchRepository: SearchRepository
) {

    /**
     * Loads next page from api
     * @return if has more images to load
     */
    fun execute(query: String): Single<Boolean> {
        return shutterStockSearchApi.search(URLEncoder.encode(query, "UTF-8"), page = START_PAGE, perPage = DEFAULT_PAGE_SIZE)
            .flatMap { response ->
                searchRepository.clearAndInsertImages(query, response.data.map { it.toEntity() })
                    .toSingleDefault(response.hasMore())
            }
    }
}
