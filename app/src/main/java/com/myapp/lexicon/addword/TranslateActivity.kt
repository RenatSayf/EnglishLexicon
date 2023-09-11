package com.myapp.lexicon.addword

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.myapp.lexicon.R
import com.myapp.lexicon.addword.TranslateFragment.Companion.getInstance
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TranslateActivity : AppCompatActivity() {

    private var translateFragment: TranslateFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.b_translate_activity)

        val sequence = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        if (sequence != null) {
            val enWord = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString().lowercase()
            translateFragment = getInstance(enWord)
            supportFragmentManager.beginTransaction()
                .add(R.id.translate_fragment, translateFragment!!)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (translateFragment != null) {
            translateFragment!!.onOptionsItemSelected(item)
        }
        return false
    }
}