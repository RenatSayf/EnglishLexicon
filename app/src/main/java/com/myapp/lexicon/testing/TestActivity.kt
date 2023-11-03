package com.myapp.lexicon.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.myapp.lexicon.auth.AuthListener
import com.myapp.lexicon.databinding.ActivityTestBinding
import com.myapp.lexicon.models.User
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
open class TestActivity : AppCompatActivity(), AuthListener {

    lateinit var binding: ActivityTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater, ConstraintLayout(this), false)
        setContentView(binding.root)
    }

    override fun refreshAuthState(user: User) {

    }
}