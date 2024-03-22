package com.myapp.lexicon.video.models

import com.myapp.lexicon.helpers.printStackTraceIfDebug
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class Bookmark(
    @SerialName("thumbnail_url")
    val thumbnailUrl: String?,
    val title: String,
    val url: String
) {

    companion object {

        @Contextual
        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun String.fromString(): Bookmark? {
            return try {
                json.decodeFromString<Bookmark>(this)
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                null
            }
        }
    }

    override fun toString(): String {

        return json.encodeToString(serializer(), this)
    }
}
