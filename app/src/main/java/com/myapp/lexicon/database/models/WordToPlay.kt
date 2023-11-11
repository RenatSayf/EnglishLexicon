package com.myapp.lexicon.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myapp.lexicon.models.Word
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Entity(tableName = "PlayList")
@Serializable
data class WordToPlay(

    @ColumnInfo(name = "_id")
    var _id: Int,

    @ColumnInfo(name = "dict_name")
    @SerialName("dict_name")
    var dictName: String,

    @PrimaryKey
    @ColumnInfo(name = "english")
    var english: String,

    @ColumnInfo(name = "translate")
    var translate: String,

    @ColumnInfo(name = "count_repeat")
    var countRepeat: Int
) {

    fun toWord(): Word {
        return Word(_id, dictName, english, translate, countRepeat)
    }
}