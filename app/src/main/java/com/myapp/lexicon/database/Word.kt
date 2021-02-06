package com.myapp.lexicon.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val _id: Int,

    @ColumnInfo(name = "dict_name")
    val dictName: String,

    @ColumnInfo(name = "english")
    val english: String,

    @ColumnInfo(name = "translate")
    val translate: String,

    @ColumnInfo(name = "count_repeat")
    val countRepeat: Int
)

