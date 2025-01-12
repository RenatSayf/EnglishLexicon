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
    fun `The user registered in the current month The result should be false`() {
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-01-09 10:06:25"
            reservedPaymentDate = ""
        }

        val actualResult = user.isResetMonthlyBalance(currentMonth = 1)
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun `The user registered at the beginning of the current month, It's the end of the current month, the result should be false`() {
        val currentTime = "2025-01-31 23:59:59"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-01-21 23:06:25"
            reservedPaymentDate = ""
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun `The user registered at the previous month, It's the start of the next month, the result should be true`() {
        val currentTime = "2025-02-01 00:00:01"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-01-21 23:06:25"
            reservedPaymentDate = ""
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)
    }

    @Test
    fun `The user registered at the previous month, It's the end of the current month, the result should be false`() {
        val currentTime = "2025-02-28 23:59:59"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-02-21 23:06:25"
            reservedPaymentDate = "2025-02-01 00:00:01"
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun `The user registered a few months ago, It's the beginning of the next month, the result should be true`() {
        val currentTime = "2025-03-01 00:00:01"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-02-21 23:06:25"
            reservedPaymentDate = "2025-02-01 00:00:01"
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)
    }

    @Test
    fun `The user registered a few months ago, It's the end of the current month, the result should be false`() {
        val currentTime = "2025-03-31 23:59:59"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-03-21 23:06:25"
            reservedPaymentDate = "2025-03-01 00:00:01"
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun `The user registered a few months ago, It's the start of the next month, the result should be true`() {
        val currentTime = "2025-04-01 00:00:01"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-03-21 23:06:25"
            reservedPaymentDate = "2025-03-01 00:00:01"
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)
    }

    @Test
    fun `The user registered in January, It's the end of April now, the result should be false`() {
        val currentTime = "2025-04-30 23:59:59"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-03-21 23:06:25" // The user did not log in in April
            reservedPaymentDate = "2025-04-01 00:00:01"
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun `The user registered in January, It's the beginning of May, and the result should be true`() {
        val currentTime = "2025-05-01 00:00:01"
        val user = User("XXX").apply {
            createdAt = "2025-01-01 00:00:01".toLongTime()
            rewardUpdateAt = "2025-05-01 00:00:01" //
            reservedPaymentDate = "2025-04-01 00:00:01"
        }
        val actualResult = user.isResetMonthlyBalance(currentMonth = currentTime.toLongTime().getMonthFromLongTime())
        Assert.assertEquals(true, actualResult)
    }


}