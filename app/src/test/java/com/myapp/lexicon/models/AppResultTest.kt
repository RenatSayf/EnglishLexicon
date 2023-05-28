package com.myapp.lexicon.models

import org.junit.Assert
import org.junit.Test

class AppResultTest {

    @Test
    fun onSuccess() {

        var result: AppResult = AppResult.Init
        Assert.assertTrue(result is AppResult.Init)

        result = AppResult.Success("XXXXXXXXXXXXXXXXXXXX")
        result.onSuccess<String> { d ->
            Assert.assertEquals(d, (result as AppResult.Success<String>).data)
        }
    }

    @Test
    fun onError() {

        var result: AppResult = AppResult.Init
        Assert.assertTrue(result is AppResult.Init)

        result = AppResult.Error(Throwable("Error"))
        result.onError { e ->
            Assert.assertEquals("Error", e.message)
        }
    }
}