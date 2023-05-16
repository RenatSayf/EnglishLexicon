package com.myapp.lexicon.testing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myapp.lexicon.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}