package com.myapp.lexicon.models

import org.json.JSONObject

data class UserPurchase(
    val productId: String,
    val purchaseToken: String,
    val purchaseTime: String,
    var developerPayload: String
) {
    companion object {
        fun fromJson(json: String): UserPurchase {
            val jsonObject = JSONObject(json)
            val id = jsonObject.getString("product_id")
            val token = jsonObject.getString("purchase_token")
            val time = jsonObject.getString("purchase_time")
            val payload = jsonObject.getString("developer_payload")

            return UserPurchase(id, token, time, payload)
        }
    }
}
