package com.rsinukov.shutterstockclient.bl.network

import com.google.gson.annotations.SerializedName

private const val FIELD_PAGE = "page"
private const val FIELD_PER_PAGE = "per_page"
private const val FIELD_TOTAL_COUNT = "total_count"
private const val FIELD_DATA = "data"
private const val FIELD_ASPECT = "aspect"
private const val FIELD_DESCRIPTION = "description"
private const val FIELD_ID = "id"
private const val FIELD_ASSETS = "assets"
private const val FIELD_WIDTH = "width"
private const val FIELD_HEIGHT = "height"
private const val FIELD_URL = "url"
private const val FIELD_PREVIEW = "preview"
private const val FIELD_SMALL_THUMB = "small_thumb"
private const val FIELD_LARGE_THUMB = "large_thumb"

val PARAM_FIELDS = """
    $FIELD_PAGE,
    $FIELD_PER_PAGE,
    $FIELD_TOTAL_COUNT,
    $FIELD_DATA(
        $FIELD_ASPECT,
        $FIELD_DESCRIPTION,
        $FIELD_ID,
        $FIELD_ASSETS(
            $FIELD_PREVIEW(
                $FIELD_URL,
                $FIELD_WIDTH,
                $FIELD_HEIGHT
            )
        )
    ),

    """.replace("\\s".toRegex(), "")

data class SearchResponse(
    @SerializedName(FIELD_PAGE) val page: Int,
    @SerializedName(FIELD_PER_PAGE) val perPage: Int,
    @SerializedName(FIELD_TOTAL_COUNT) val totalCount: Int,
    @SerializedName(FIELD_DATA) val data: List<Data>
)

data class Data(
    @SerializedName(FIELD_ASPECT) val aspect: Float,
    @SerializedName(FIELD_DESCRIPTION) val description: String,
    @SerializedName(FIELD_ID) val id: String,
    @SerializedName(FIELD_ASSETS) val assets: Assets
)

data class Assets(
    @SerializedName(FIELD_PREVIEW) val preview: Asset
)

data class Asset(
    @SerializedName(FIELD_WIDTH) val width: Int,
    @SerializedName(FIELD_HEIGHT) val height: Int,
    @SerializedName(FIELD_URL) val url: String
)

fun SearchResponse.hasMore(): Boolean = totalCount > page * perPage
