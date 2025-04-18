package com.myapp.lexicon.ads.native_ad

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.native_ad.NativeAdsActivity.Listener
import com.myapp.lexicon.databinding.ActivityNativeAdsXBinding

class NativeAdsActivityX : AppCompatActivity() {

    companion object {

        const val AD_ID = "AD_ID_785219534"

        private var listener: Listener? = null
        fun setAdDataListener(listener: Listener) {
            this.listener = listener
        }
    }

    private var binding: ActivityNativeAdsXBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNativeAdsXBinding.inflate(layoutInflater)
        if (binding != null) {
            setContentView(binding!!.root)
        } else {
            finish()
        }

        with(binding!!) {

        }

    }

    override fun onDestroy() {

        binding = null
        listener = null

        super.onDestroy()
    }
}