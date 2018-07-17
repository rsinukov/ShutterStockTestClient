package com.rsinukov.shutterstockclient.bl.storage

data class Image(
    val aspect: Float,
    val description: String,
    val id: String,
    val preview: Asset
)

data class Asset(
    val width: Int,
    val height: Int,
    val url: String
)

