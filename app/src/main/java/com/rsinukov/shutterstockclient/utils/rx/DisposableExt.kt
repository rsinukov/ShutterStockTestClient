package com.rsinukov.shutterstockclient.utils.rx

import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer

operator fun DisposableContainer.plusAssign(disposable: Disposable) {
    this.add(disposable)
}

operator fun DisposableContainer.minusAssign(disposable: Disposable) {
    this.delete(disposable)
}

fun <T : Disposable> T.addTo(container: DisposableContainer): T = apply {
    container.add(this)
}
