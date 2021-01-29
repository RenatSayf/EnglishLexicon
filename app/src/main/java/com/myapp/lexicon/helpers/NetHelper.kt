package com.myapp.lexicon.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetHelper
{
    fun isOnline(context: Context): Boolean
    {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var netInfo: NetworkInfo? = null
        netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}