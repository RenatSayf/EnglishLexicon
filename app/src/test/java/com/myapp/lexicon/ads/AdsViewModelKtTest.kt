package com.myapp.lexicon.ads

import org.junit.Assert
import org.junit.Test


class AdsViewModelKtTest {

    @Test
    fun jsonToAdData() {
        val jsonString = """{
  "currency": "RUB",
  "revenueUSD": "0.001200671",
  "precision": "estimated",
  "revenue": "0.116025434",
  "requestId": "1694954665976270-617871108186477874100342-production-app-host-vla-326",
  "blockId": "R-M-711877-3",
  "adType": "interstitial",
  "ad_unit_id": "R-M-711877-3",
  "network": {
    "name": "Yandex",
    "adapter": "Yandex",
    "ad_unit_id": "R-M-711877-3"
  }
}"""
        jsonString.toAdData(
            onSuccess = {adData ->
                Assert.assertEquals(0.001200671, adData.revenueUSD, 0.01)
                Assert.assertEquals(0.116025434, adData.revenue, 0.01)
                Assert.assertEquals("R-M-711877-3", adData.network?.adUnitId)
            },
            onFailed = {
                Assert.assertTrue(false)
            }
        )

    }
}