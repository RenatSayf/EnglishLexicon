package com.myapp.lexicon.testing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.ActivityTestBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater, ConstraintLayout(this), false)
        setContentView(binding.root)
    }
}