package com.myapp.lexicon.ads.models

enum class AdType(val type: Int) {
    NATIVE(type = 1),
    INTERSTITIAL(type = 2),
    REWARDED(type = 3),
    FEED(type = 4)
}

