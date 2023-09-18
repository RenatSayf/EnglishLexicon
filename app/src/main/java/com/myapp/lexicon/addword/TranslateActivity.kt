package com.myapp.lexicon.addword

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.myapp.lexicon.R
import com.myapp.lexicon.addword.TranslateFragment.Companion.getInstance
import com.myapp.lexicon.ads.intrefaces.AdEventListener
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.main.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TranslateActivity : AppCompatActivity(), AdEventListener {

    private var translateFragment: TranslateFragment? = null
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val userVM: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.b_translate_activity)

        val sequence = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        if (sequence != null) {
            val enWord = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString().lowercase()
            translateFragment = getInstance(enWord, this)
            supportFragmentManager.beginTransaction()
                .add(R.id.translate_fragment, translateFragment!!)
                .addToBackStack(null)
                .commit()
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            userVM.getUserFromCloud(currentUser.uid)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (translateFragment != null) {
            translateFragment!!.onOptionsItemSelected(item)
        }
        return false
    }

    override fun onAdImpression(data: AdData?) {
        if (data != null) {
            val user = userVM.user.value
            if (user != null) {
                val revenue = data.revenue
                val currency = data.currency
                user.totalRevenue = revenue
                user.currency = currency
                userVM.updateUserRevenue(revenue, user)
                finish()
            }
        }
        else {
            finish()
        }
    }
}