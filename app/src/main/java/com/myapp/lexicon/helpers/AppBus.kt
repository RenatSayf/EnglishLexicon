package com.myapp.lexicon.helpers

import com.myapp.lexicon.database.Word
import io.reactivex.subjects.BehaviorSubject

object AppBus
{
    private val wordsToUpdate: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val wordToPass: BehaviorSubject<Word> = BehaviorSubject.create()

    fun updateWords(flag: Boolean)
    {
        wordsToUpdate.onNext(flag)
    }

    fun isRefresh() = wordsToUpdate

    fun passWord(word: Word)
    {
        wordToPass.onNext(word)
    }

    fun getWord() = wordToPass
}