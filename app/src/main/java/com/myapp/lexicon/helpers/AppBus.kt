package com.myapp.lexicon.helpers

import io.reactivex.subjects.BehaviorSubject
import kotlin.collections.ArrayList

object AppBus
{
    private val wordsToUpdate: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun updateWords(flag: Boolean)
    {
        wordsToUpdate.onNext(flag)
    }

    fun isRefresh() = wordsToUpdate
}