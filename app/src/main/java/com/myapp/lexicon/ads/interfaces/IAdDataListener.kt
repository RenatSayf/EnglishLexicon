package com.myapp.lexicon.ads.interfaces

import com.myapp.lexicon.ads.models.AdData

interface IAdDataListener {

    fun onImpression(data: AdData?)
}