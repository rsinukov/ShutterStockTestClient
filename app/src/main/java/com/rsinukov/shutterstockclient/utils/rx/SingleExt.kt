package com.rsinukov.shutterstockclient.utils.rx

import io.reactivex.Observable
import io.reactivex.Single

inline fun <reified T> Single<out T>.toLoadingStateObservable(started: T, finished: T, failed: T): Observable<T> {
    return toObservable()
        .cast(T::class.java)
        .startWith(started)
        .onErrorReturn { failed }
        .concatWith(Observable.just(finished))
}
