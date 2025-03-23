package com.myapp.lexicon.models

import kotlinx.serialization.json.Json
import org.junit.Assert
import kotlin.test.Test

class UserTest {

    private val jsonDecoder = Json(builderAction = {
        ignoreUnknownKeys
        explicitNulls
    })

    @Test
    fun fromJson_all_data() {

        val userJson = """{
  "email": "user-test@mail.com",
  "today_balance": 10.0,
  "yesterday_balance": 20.0,
  "month_balance": 100.0,
  "previous_month_balance": 200,
  "reserved_payout": 300,
  "currency_code": "RUB",
  "phone": "+79998887755",
  "first_name": "Кутман",
  "second_name": "Уулу",
  "last_name": "Бекмурза",
  "bank_name": "Сбербанк",
  "message_to_user": "Тестовое сообщение"
}""".trimIndent()

        val user = jsonDecoder.decodeFromString<UserX>(userJson)
        Assert.assertEquals("user-test@mail.com", user.email)
        Assert.assertEquals("", user.bankCard)
    }

    @Test
    fun fromJson_email_only() {

        val userJson = """{
  "email": "user-test@mail.com"
}""".trimIndent()

        val user = jsonDecoder.decodeFromString<UserX>(userJson)
        Assert.assertEquals("user-test@mail.com", user.email)
        Assert.assertEquals("", user.bankCard)
    }
}