package com.rsinukov.shutterstockclient.bl.usecases

import com.rsinukov.shutterstockclient.bl.network.Data
import com.rsinukov.shutterstockclient.bl.storage.Asset
import com.rsinukov.shutterstockclient.bl.storage.Image

internal fun Data.toEntity(): Image {
    return Image(
        id = id,
        aspect = aspect,
        description = description,
        preview = Asset(
            width = assets.preview.width,
            height = assets.preview.height,
            url = assets.preview.url
        )
    )
}
