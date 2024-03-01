package com.myapp.lexicon.video.models.query

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity(tableName = "History")
@Serializable
data class HistoryQuery(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "video_id")
    val videoId: String,

    @ColumnInfo(name = "viewing_time")
    val viewingTime: Long,

    @ColumnInfo(name = "text")
    override val text: String,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String,

    @ColumnInfo(name = "page_token")
    val pageToken: String

): ISearchItem