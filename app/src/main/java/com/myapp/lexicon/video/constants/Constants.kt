package com.myapp.lexicon.video.constants

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.video.models.Bookmark
import kotlinx.serialization.json.Json


private val jsonDecoder = Json { ignoreUnknownKeys = true }

val bookmarks: List<Bookmark>
    get() {
        return try {
            val json = Firebase.remoteConfig.getString("video_references")
            val referenceList = jsonDecoder.decodeFromString<List<Bookmark>>(json)
            referenceList
        } catch (e: Exception) {
            emptyList()
        }
    }