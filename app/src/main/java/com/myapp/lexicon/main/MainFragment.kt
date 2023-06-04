@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.myapp.lexicon.R

class MainFragment : Fragment() {

    companion object {

        private var instance: MainFragment? = null
        private var listener: Listener? = null

        fun getInstance(listener: Listener): MainFragment {

            this.listener = listener
            return if (instance == null) {
                instance = MainFragment()
                instance!!
            }
            else {
                instance!!
            }
        }
    }

    interface Listener {
        fun refreshMainScreen(isAdShow: Boolean)
        fun onVisibleMainScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(getString(R.string.KEY_NEED_REFRESH), listener = { requestKey, bundle ->
            if (requestKey == getString(R.string.KEY_NEED_REFRESH)) {
                listener?.refreshMainScreen(false)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        listener?.onVisibleMainScreen()
    }

}