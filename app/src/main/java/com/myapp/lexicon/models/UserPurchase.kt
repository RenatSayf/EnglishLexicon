package com.myapp.lexicon.models

import org.json.JSONObject

data class UserPurchase(
    val orderId: String,
    val productId: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    var quantity: Int = 0,
    var acknowledged: Boolean = false
) {
    companion object {
        fun fromJson(json: String): UserPurchase {
            val jsonObject = JSONObject(json)
            val orderId = jsonObject.getString("orderId")
            val productId = jsonObject.getString("productId")
            val token = jsonObject.getString("purchaseToken")
            val time = jsonObject.getLong("purchaseTime")
            val quantity = jsonObject.getInt("quantity")
            val acknowledged = jsonObject.getBoolean("acknowledged")

            return UserPurchase(orderId, productId, token, time, quantity, acknowledged)
        }
    }
}
