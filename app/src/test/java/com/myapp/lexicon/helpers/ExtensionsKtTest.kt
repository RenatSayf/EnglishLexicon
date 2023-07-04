package com.myapp.lexicon.helpers

import org.junit.Assert
import org.junit.Test
import java.util.Locale


class ExtensionsKtTest {


    @Test
    fun getCRC32CheckSum() {

        val actualCheckSum =
            "bjppaehkgdeldlcnmhnmiaeo.AO-J1OyRVjp1kZ3A2yGhxnfeDKo14VHnix1KJnN-xq5Ee_wFGtzp9YDkSCveWgRaUJ088qhvezU97VSL4_kq_d1u-Nwax1s_MQ/".getCRC32CheckSum()
        Assert.assertEquals(2100595828L, actualCheckSum)
    }

    @Test
    fun getCRC32CheckSum_empty_string() {

        val actualCheckSum = "".getCRC32CheckSum()
        Assert.assertEquals(0L, actualCheckSum)
    }

    @Test
    fun toStringDate() {
        val timeMillis: Long = 1688378419048
        val actualDate = timeMillis.toStringDate(Locale("ru", "RU"))
        Assert.assertEquals("2023-07-03", actualDate)
    }

    @Test
    fun toLongDate_Success() {
        val dataStr = "2023-07-03"
        val actualResult = dataStr.toLongDate()
        Assert.assertEquals(1688324400000L, actualResult)
    }

    @Test
    fun toLongDate_Wrong_input() {
        val dataStr = "Xxxxxx"
        val actualResult = dataStr.toLongDate()
        Assert.assertTrue(actualResult < 0)
    }

    @Test
    fun toLongDate_Compare_two_dates() {
        val dataStr = "2023-07-03"
        val actualResult = dataStr.toLongDate()
        val currentTimeMillis = System.currentTimeMillis()
        Assert.assertTrue(actualResult < currentTimeMillis)
    }
}