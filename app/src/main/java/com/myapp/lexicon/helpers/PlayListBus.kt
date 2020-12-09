package com.myapp.lexicon.helpers

import io.reactivex.subjects.BehaviorSubject
import java.util.*
import kotlin.collections.ArrayList

object PlayListBus
{
    private val playList: BehaviorSubject<ArrayList<String>> = BehaviorSubject.create()

    fun update(any: ArrayList<String>)
    {
        playList.onNext(any)
    }

    fun toObservable() = playList
}