package com.myapp.lexicon.repository.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.myapp.lexicon.video.models.VideoSearchResult
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class NetRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun before() {
    }
    @After
    fun after() {
    }

    @Test
    fun decodeSearchResult() {
        val result = Json.decodeFromString<VideoSearchResult>(TEST_VIDEO_LIST)
        Assert.assertTrue(result.videoItems.isNotEmpty())
    }
}