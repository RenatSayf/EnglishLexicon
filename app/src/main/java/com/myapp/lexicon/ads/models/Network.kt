@file:Suppress("PropertyName")

package com.myapp.lexicon.ads.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Network(
    @SerialName("ad_unit_id")
    val adUnitId: String?,
    val adapter: String?,
    val name: String?
)