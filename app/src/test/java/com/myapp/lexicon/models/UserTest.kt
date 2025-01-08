package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.getMonthFromLongTime
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

    @Test
    fun isResetMonthlyBalance() {

        val user = User("XXX").apply {
            createdAt = "2025-01-08 00:00:01".toLongTime()
            rewardUpdateAt = "2025-01-09 10:06:25"
            reservedPaymentDate = ""
        }

        var actualResult = user.isResetMonthlyBalance(currentMonth = 1)
        Assert.assertEquals(false, actualResult)

        var currentTime = "2025-01-31 23:59:59"
        user.apply {
            createdAt = "2025-01-08 00:00:01".toLongTime()
            rewardUpdateAt = currentTime
            reservedPaymentDate = ""
        }
        actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(false, actualResult)

        currentTime = "2025-02-01 00:00:01"
        user.apply {
            createdAt = "2025-01-08 00:00:01".toLongTime()
            rewardUpdateAt = "2025-01-31 23:59:59"
            reservedPaymentDate = ""
        }
        actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)

        currentTime = "2025-02-01 00:00:01"
        user.apply {
            createdAt = "2025-01-08 00:00:01".toLongTime()
            rewardUpdateAt = currentTime
            reservedPaymentDate = ""
        }
        actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)

        currentTime = "2025-02-28 23:59:59"
        user.apply {
            createdAt = "2025-01-08 00:00:01".toLongTime()
            rewardUpdateAt = currentTime
            reservedPaymentDate = "2025-02-01 00:00:01"
        }
        actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(false, actualResult)

        currentTime = "2025-03-01 00:00:01"
        user.apply {
            createdAt = "2025-01-08 00:00:01".toLongTime()
            rewardUpdateAt = currentTime
            reservedPaymentDate = "2025-02-01 00:00:01"
        }
        actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)

        currentTime = "2025-04-01 00:00:01"
        user.apply {
            createdAt = "2025-01-08 00:00:01".toLongTime()
            rewardUpdateAt = "2025-03-01 00:00:01"
            reservedPaymentDate = "2025-02-01 00:00:01"
        }
        actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)
    }


}