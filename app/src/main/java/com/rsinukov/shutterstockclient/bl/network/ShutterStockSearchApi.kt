package com.rsinukov.shutterstockclient.bl.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ShutterStockSearchApi {

    @GET("images/search/")
    fun search(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("fields") fields: String = PARAM_FIELDS
    ): Single<SearchResponse>
}
