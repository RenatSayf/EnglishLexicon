package com.myapp.lexicon.video.constants

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.video.models.Bookmark
import com.parse.ParseConfig
import kotlinx.serialization.json.Json


private val jsonDecoder = Json { ignoreUnknownKeys = true }

val BOOKMARKS: List<Bookmark>
    get() {
        return try {
            val json = Firebase.remoteConfig.getString("video_references")
            val referenceList = jsonDecoder.decodeFromString<List<Bookmark>>(json)
            referenceList
        } catch (e: Exception) {
            emptyList()
        }
    }

var IS_VIDEO_SECTION: Boolean = true

var VIDEO_URL: String = "https://m.youtube.com/"

var PRETTY_PRINT_URL: String = "https://m.youtube.com/youtubei/v1/player?prettyPrint=false"