@file:Suppress("PropertyName", "UnnecessaryVariable")

package com.myapp.lexicon.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Entity(tableName = "Words")
@Serializable
data class Word(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Int,

    @ColumnInfo(name = "dict_name")
    var dictName: String,

    @ColumnInfo(name = "english")
    var english: String,

    @ColumnInfo(name = "translate")
    var translate: String,

    @ColumnInfo(name = "count_repeat")
    var countRepeat: Int
)
{
    override fun toString(): String {
        val json = Json.encodeToString(serializer(), this)
        return json
    }

}

fun String.toWord(): Word {
    val word = Json.decodeFromString<Word>(this)
    return word
}

fun List<Word>.toWordsString(): String {
    val json = Json.encodeToString(serializer(), this)
    return json
}

fun String.toWordList(): List<Word>? {
    val wordList = try {
        Json.decodeFromString<List<Word>>(this)
    } catch (e: Exception) {
        e.printStackTraceIfDebug()
        null
    }
    return wordList
}