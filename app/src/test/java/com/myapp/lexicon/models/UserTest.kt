package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.toLongTime
import org.junit.Assert
import org.junit.Test


class UserTest {


    @Test
    fun checkPayDateIsLastMonth_true() {
        val user = User("NNN").apply {
            paymentDate = "2024-06-30 23:59:59"
        }
        val currentTime = "2024-07-01 00:00:01".toLongTime()
        val actualResult = user.checkPayDateIsLastMonth(currentTime = currentTime)
        Assert.assertEquals(true, actualResult)
    }

    @Test
    fun checkPayDateIsLastMonth_false() {
        val user = User("NNN").apply {
            paymentDate = "2024-07-12 23:59:59"
        }
        val currentTime = "2024-07-20 00:00:01".toLongTime()
        val actualResult = user.checkPayDateIsLastMonth(currentTime = currentTime)
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun checkPayDateIsLastMonth_empty_date() {
        val user = User("NNN").apply {
            paymentDate = ""
        }
        val currentTime = "2024-07-20 00:00:01".toLongTime()
        val actualResult = user.checkPayDateIsLastMonth(currentTime = currentTime)
        Assert.assertEquals(true, actualResult)
    }


}