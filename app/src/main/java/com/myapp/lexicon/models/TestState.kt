package com.myapp.lexicon.models

import java.io.Serializable

data class TestState(
    var dict: String = "",
    var wordId: Int = 0,
    var progress: Int = 0,
    var progressMax: Int = Int.MAX_VALUE,
    var rightAnswers: Int = 0,
    var studiedWordIds: MutableList<Int> = mutableListOf()
): Serializable {

    companion object {
        const val KEY_DICTIONARY = "KEY_DICTIONARY"
        const val KEY_WORD_ID = "WORD_ID"
        const val KEY_PROGRESS = "PROGRESS"
        const val KEY_PROGRESS_MAX = "PROGRESS_MAX"
        const val KEY_RIGHT_ANSWERS = "RIGHT_ANSWERS"
        const val KEY_STUDIED_WORD_IDS = "STUDIED_WORD_IDS"
    }

    fun reset(progressMax: Int = Int.MAX_VALUE): TestState {
        return this.apply {
            dict = ""
            wordId = 0
            progress = 0
            this.progressMax = progressMax
            rightAnswers = 0
            studiedWordIds = mutableListOf()
        }
    }
}

