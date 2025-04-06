package com.myapp.lexicon.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.myapp.lexicon.ads.banner.BannerAdIds
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository
import kotlinx.serialization.Serializable


class RemoteConfigViewModel(
    private val app: Application,
    private val netModule: INetRepositoryModule
) : AndroidViewModel(app) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val app: Application = App(),
        private val netModule: INetRepositoryModule = NetRepositoryModule()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == RemoteConfigViewModel::class.java)
            return RemoteConfigViewModel(app, netModule) as T
        }
    }

    sealed interface State {
        data class RemoteConfigLoaded(val config: Config)
    }

    private var _state = MutableLiveData<Config>()
    val state: LiveData<Config> = _state

    private val repository: INetRepository = netModule.apply {
        setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                netModule.setRefreshToken(tokens.refreshToken)
                app.saveAuthTokens(tokens)
            }

            override fun onAuthorizationRequired() {

            }
        })
        setRefreshToken(app.refreshToken)
    }.provideNetRepository()

    private val pref: SharedPreferences
        get() {
            return PreferenceManager.getDefaultSharedPreferences(app)
        }

    @Serializable
    data class Config(
        val adTypePerScreen: Map<String, Int> = mapOf(
            AD_MAIN to 1,
            AD_SERVICE to 1,
            AD_TEST to 3,
            AD_TRANSLATE to 1,
            AD_VIDEO to 3
        ),
        val bannerIds: Map<String, String> = mapOf(
            "BANNER_MAIN" to BannerAdIds.BANNER_1.id,
            "BANNER_TRANSLATE" to BannerAdIds.BANNER_2.id,
            "BANNER_SERVICE" to BannerAdIds.BANNER_1.id,
            "BANNER_EDITOR" to BannerAdIds.BANNER_3.id
        ),
        val nativeIds: Map<String, String> = mapOf(

        )
    ) {
        companion object {
            const val AD_MAIN = "AD_MAIN"
            const val AD_SERVICE = "AD_SERVICE"
            const val AD_TEST = "AD_TEST"
            const val AD_TRANSLATE = "AD_TRANSLATE"
            const val AD_VIDEO = "AD_VIDEO"
        }
    }
}




