package com.myapp.lexicon.helpers

import org.junit.Assert
import org.junit.Test


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
}