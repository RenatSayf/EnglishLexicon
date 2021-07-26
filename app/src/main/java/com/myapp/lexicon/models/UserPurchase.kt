package com.myapp.lexicon.models

data class UserPurchase(
    val productId: String,
    val purchaseToken: String,
    val purchaseTime: String,
    var developerPayload: String
)
