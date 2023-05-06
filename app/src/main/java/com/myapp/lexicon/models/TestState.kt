package com.myapp.lexicon.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TestState(
    var dict: String = "",

    @SerializedName("word_id")
    var wordId: Int = 0,

    var progress: Int = 0,

    @SerializedName("progress_max")
    var progressMax: Int = Int.MAX_VALUE,

    @SerializedName("right_answers")
    var rightAnswers: Int = 0,

    @SerializedName("studied_word_ids")
    var studiedWordIds: MutableList<Int> = mutableListOf()
): Serializable {

    fun reset(): TestState {
        return this.apply {
            dict = ""
            wordId = 0
            progress = 0
            progressMax = Int.MAX_VALUE
            rightAnswers = 0
            studiedWordIds = mutableListOf()
        }
    }
}
