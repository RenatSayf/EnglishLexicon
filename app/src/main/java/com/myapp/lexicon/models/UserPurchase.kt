package com.myapp.lexicon.models

import org.json.JSONException
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
            val orderId = try {
                jsonObject.getString("orderId")
            } catch (e: JSONException) {
                ""
            }
            val productId = try {
                jsonObject.getString("productId")
            } catch (e: JSONException) {
                ""
            }
            val token = try {
                jsonObject.getString("purchaseToken")
            } catch (e: JSONException) {
                ""
            }
            val time = try {
                jsonObject.getLong("purchaseTime")
            } catch (e: JSONException) {
                0
            }
            val quantity = try {
                jsonObject.getInt("quantity")
            } catch (e: JSONException) {
                0
            }
            val acknowledged = try {
                jsonObject.getBoolean("acknowledged")
            } catch (e: JSONException) {
                true
            }

            return UserPurchase(orderId, productId, token, time, quantity, acknowledged)
        }
    }
}
