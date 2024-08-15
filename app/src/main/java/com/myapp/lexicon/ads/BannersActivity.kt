package com.myapp.lexicon.ads

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.ActivityBannersBinding

class BannersActivity : Activity() {

    companion object {
        const val ARG_ID_LIST = "BANNERS_IDS"
    }

    private var binding: ActivityBannersBinding? = null

    interface Listener {
        fun onDismissed(data: AdData?)
    }

    private var listener: Listener? = null
    private var adData: AdData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityBannersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        with(binding) {

        }

    }

    override fun onDestroy() {

        binding = null
        super.onDestroy()
    }
}