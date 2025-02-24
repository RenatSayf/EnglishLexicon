package com.myapp.lexicon.ads.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class AdName {
    FULL_MAIN,
    FULL_TRANSLATE,
    FULL_TEST,
    FULL_VIDEO,
    FULL_SERVICE,
    BANNER_MAIN,
    BANNER_TRANSLATE,
    BANNER_EDITOR,
    BANNER_SERVICE
}

@Serializable
data class AdData(
    var adType: String? = null,
    @SerialName("ad_unit_id")
    var adUnitId: String? = null,
    var blockId: String? = null,
    var currency: String? = null,
    var network: Network? = null,
    var precision: String? = null,
    var requestId: String? = null,
    var revenue: Double = 0.0,
    var revenueUSD: Double = 0.0
) : java.io.Serializable {

    companion object {
        fun String.fromString(): AdData? {
            return try {
                Json.decodeFromString<AdData>(this)
            } catch (e: Exception) {
                null
            }
        }
    }

    var adCount = emptyMap<String, Int>()

    override fun toString(): String {
        return try {
            Json.encodeToString(serializer(), this)
        } catch (e: Exception) {
            ""
        }
    }
}