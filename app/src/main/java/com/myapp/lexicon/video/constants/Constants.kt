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

fun initRemoteConfig() {
    try {
        ParseConfig.get().apply {
            IS_VIDEO_SECTION = getBoolean("is_video_section")
            PRETTY_PRINT_URL = getString("pretty_print")
            VIDEO_URL = getString("video_url")
        }
    } catch (e: Exception) {
        e.printStackTraceIfDebug()
    }
}

var IS_VIDEO_SECTION: Boolean = try {
    val remoteKey = "is_video_section"
    var currentValue = ParseConfig.getCurrentConfig().getBoolean(remoteKey)
    if (!currentValue) {
        currentValue = ParseConfig.get().getBoolean(remoteKey)
    }
    currentValue
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    false
}

var VIDEO_URL: String = try {
    val remoteKey = "video_url"
    var currentValue = ParseConfig.getCurrentConfig().getString(remoteKey)
    if (currentValue == null) {
        currentValue = ParseConfig.get().getString(remoteKey)
    }
    currentValue
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    ""
}

var PRETTY_PRINT_URL: String = try {
    val remoteKey = "pretty_print"
    var currentValue = ParseConfig.getCurrentConfig().getString(remoteKey)
    if (currentValue == null) {
        currentValue = ParseConfig.get().getString(remoteKey)
    }
    currentValue
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    ""
}