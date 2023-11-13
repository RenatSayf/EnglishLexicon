package com.myapp.lexicon.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.Serializable


@Entity(tableName = "Counters")
@Serializable
data class Counters(

    @ColumnInfo(name = "row_num")
    val rowNum: Int,

    @ColumnInfo(name = "total_count")
    val count: Int,

    @ColumnInfo(name = "unused")
    val unUsed: Int
)
