package com.myapp.lexicon.ads.intrefaces

import com.myapp.lexicon.ads.models.AdData

interface AdEventListener {
    fun onAdImpression(data: AdData?)
}