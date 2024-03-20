package com.myapp.lexicon.video.constants

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.video.models.VideoReference
import kotlinx.serialization.json.Json


private val jsonDecoder = Json { ignoreUnknownKeys = true }

val videoReferences: List<VideoReference>
    get() {
        return try {
            val json = Firebase.remoteConfig.getString("video_references")
            val referenceList = jsonDecoder.decodeFromString<List<VideoReference>>(json)
            referenceList
        } catch (e: Exception) {
            emptyList()
        }
    }