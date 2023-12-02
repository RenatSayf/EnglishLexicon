package com.myapp.lexicon.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.myapp.lexicon.databinding.ActivityTestBinding


open class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater, ConstraintLayout(this), false)
        setContentView(binding.root)
    }

}