package com.myapp.lexicon.video.constants

import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.video.models.Bookmark
import kotlinx.serialization.json.Json


private val jsonDecoder = Json { ignoreUnknownKeys = true }

val BOOKMARKS: List<Bookmark>
    get() {
        return try {
            val json = """[
  {
    "thumbnail_url": "https://i.ytimg.com/vi/sicBNCh-Yxc/hqdefault.jpg?sqp=-oaymwEjCNACELwBSFryq4qpAxUIARUAAAAAGAElAADIQj0AgKJDeAE=&rs=AOn4CLDDhrzEq1zuHc5yLRL0eRTDW2OrPw",
    "title": "РАЗГОВОРНЫЙ АНГЛИЙСКИЙ с сериалом ДРУЗЬЯ",
    "url": "https://m.youtube.com/watch?v=sicBNCh-Yxc"
  },
  {
    "thumbnail_url": "https://i.ytimg.com/vi/muWKxCYA4uU/mqdefault.jpg",
    "title": "Порядок слов в английских предложениях для начинающих || Puzzle English",
    "url": "https://m.youtube.com/watch?v=muWKxCYA4uU"
  },
  {
    "thumbnail_url": "https://i.ytimg.com/vi/MbQ_B4eWOZc/mqdefault.jpg",
    "title": "Все про артикли. Часть 1 || Puzzle English",
    "url": "https://m.youtube.com/watch?v=MbQ_B4eWOZc"
  },
  {
    "thumbnail_url": "https://i.ytimg.com/vi/dH1XDqIOQ80/mqdefault.jpg",
    "title": "Все про артикли. Часть 2 || Puzzle English",
    "url": "https://m.youtube.com/watch?v=dH1XDqIOQ80"
  },
  {
    "thumbnail_url": "https://i.ytimg.com/vi/S5EZJv_Gaxc/mqdefault.jpg",
    "title": "Все про артикли. Часть 3 || Puzzle English",
    "url": "https://m.youtube.com/watch?v=S5EZJv_Gaxc"
  },
  {
    "thumbnail_url": "https://i.ytimg.com/vi/VpKU1O1Og38/hqdefault.jpg?sqp=-oaymwEcCNACELwBSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLDfWBgx9B0DgTZkhYX60AakXFa93w",
    "title": "АНГЛИЙСКИЙ ПО ПЕСНЯМ - Red Hot Chili Peppers: Californication",
    "url": "https://m.youtube.com/watch?v=VpKU1O1Og38&t=906s"
  }
]"""
            val referenceList = jsonDecoder.decodeFromString<List<Bookmark>>(json)
            referenceList
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            emptyList()
        }
    }

var IS_VIDEO_SECTION: Boolean = true

var VIDEO_URL: String = "https://m.youtube.com/"

var PRETTY_PRINT_URL: String = "https://m.youtube.com/youtubei/v1/player?prettyPrint=false"