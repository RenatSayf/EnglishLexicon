package com.myapp.lexicon.video.web.pref

import android.content.Context
import com.myapp.lexicon.settings.appSettings


var Context.lastUrl: String?
    get() {
        return this.appSettings.getString("KEY_LAST_URL", null)
    }
    set(value) {
        this.appSettings.edit().putString("KEY_LAST_URL", value).apply()
    }