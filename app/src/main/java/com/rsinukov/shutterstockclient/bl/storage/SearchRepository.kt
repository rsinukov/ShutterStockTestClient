package com.rsinukov.shutterstockclient.bl.storage

import com.rsinukov.shutterstockclient.dagger.IoScheduler
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

// TODO move from in memory storage to persistent
@Singleton
class SearchRepository @Inject constructor(
    @IoScheduler private val scheduler: Scheduler
) {

    private val imagesSubject = BehaviorSubject
        .createDefault(ConcurrentHashMap<String, List<Image>>())
        .toSerialized()

    fun observeImages(search: String, limit: Int): Observable<List<Image>> = imagesSubject.hide()
        .observeOn(scheduler)
        .map {
            val images = it[search].orEmpty()
            images.subList(0, min(images.size, limit))
        }

    fun insertImages(search: String, images: List<Image>): Completable = Completable.fromCallable {
        val currentMap = imagesSubject.blockingFirst()
        currentMap[search] = currentMap[search].orEmpty() + images
        this.imagesSubject.onNext(currentMap)
    }

    fun clearAndInsertImages(search: String, images: List<Image>): Completable = Completable.fromCallable {
        val currentMap = imagesSubject.blockingFirst()
        currentMap[search] = images
        this.imagesSubject.onNext(currentMap)
    }
}
