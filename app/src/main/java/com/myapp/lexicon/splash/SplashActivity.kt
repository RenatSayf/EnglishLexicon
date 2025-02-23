package com.myapp.lexicon.splash

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AppOpenAdViewModel
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.common.KEY_APP_STORE_LINK
import com.myapp.lexicon.common.MESSAGE_TO_USER
import com.myapp.lexicon.databinding.ALayoutSplashScreenBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.showDialogAsSingleton
import com.myapp.lexicon.helpers.startTimer
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.Speaker
import com.myapp.lexicon.settings.adsIsEnabled
import com.myapp.lexicon.settings.checkOnStartSpeech
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.myapp.lexicon.settings.goToAppStore
import com.myapp.lexicon.settings.isUserRegistered
import com.yandex.mobile.ads.appopenad.AppOpenAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        const val NEED_UPDATE_USER_REWARD = 147852324
    }

    private lateinit var binding: ALayoutSplashScreenBinding
    private var speaker: Speaker? = null

    private var appOpenAd: AppOpenAd? = null
    private val authVM: AuthViewModel by viewModels()
    private val openAdVM: AppOpenAdViewModel by viewModels()

    private var authChecked = false
    private var speechChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ALayoutSplashScreenBinding.inflate(layoutInflater, CoordinatorLayout(this), false)
        setContentView(binding.root)

        IS_REWARD_ACCESSIBLE
        MESSAGE_TO_USER

        val extras = intent.extras
        val appStoreLink = extras?.getString(KEY_APP_STORE_LINK)
        if (!appStoreLink.isNullOrEmpty()) {
            goToAppStore()
            finish()
        }

        applicationContext.getAuthDataFromPref(
            onNotRegistered = {
                authChecked = true
            },
            onSuccess = { email, password ->
                authVM.signInWithEmailAndPassword(email, password)
            }
        )

        authVM.state.observe(this) { state ->
            state.onSignIn { user ->
                this.adsIsEnabled = user.isAdsEnabled
                authChecked = true
            }
            state.onFailure {
                authChecked = true
            }
        }

        openAdVM.resultOpenAd.observe(this) { result ->
            result.onSuccess { ad: AppOpenAd ->
                appOpenAd = ad
            }
        }
    }

    override fun onResume() {
        super.onResume()

        speaker = Speaker(this, object : Speaker.Listener {
            override fun onSuccessInit() {
                this@SplashActivity.checkOnStartSpeech(
                    onEnabled = {
                        speaker?.doSpeech(getString(R.string.start_speech_en), Locale.US)
                    },
                    onDisabled = {
                        speechChecked = true
                    }
                )
            }

            override fun onSpeechStart(id: String?) {
                return
            }

            override fun onSpeechDone(id: String?) {
                speechChecked = true
                return
            }

            override fun onSpeechError(id: String?) {
                speechChecked = true
                return
            }

            override fun onSpeechInitNotSuccess(status: Int) {

                this@SplashActivity.showDialogAsSingleton(ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
                    with(binding) {
                        dialog.isCancelable = false
                        tvMessage.text = getString(R.string.message_inst_tts_engine)
                        ivIcon.setImageResource(R.drawable.ic_warning)
                        btnOk.apply {
                            text = getString(R.string.btn_text_setup)
                            setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(getString(R.string.url_google_tts))
                                }
                                startActivity(intent)
                                dialog.dismiss()
                            }
                        }
                        btnCancel.apply {
                            text = getString(R.string.btn_text_continue)
                            setOnClickListener {
                                speechChecked = true
                                dialog.dismiss()
                            }
                        }
                    }
                }), ConfirmDialog.TAG)
            }

            override fun onEngLangNotSupported(status: Int) {
                this@SplashActivity.showDialogAsSingleton(ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
                    with(binding) {
                        dialog.isCancelable = false
                        tvMessage.text = getString(R.string.message_inst_tts_data)
                        ivIcon.setImageResource(R.drawable.ic_warning)
                        btnOk.apply {
                            text = getString(R.string.btn_text_setup)
                            setOnClickListener {
                                val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                startActivity(intent)
                                dialog.dismiss()
                            }
                        }
                        btnCancel.apply {
                            text = getString(R.string.btn_text_continue)
                            setOnClickListener {
                                speechChecked = true
                                dialog.dismiss()
                            }
                        }
                    }
                }), ConfirmDialog.TAG)
            }

            override fun onRusLangNotSupported(status: Int) {

                this@SplashActivity.showDialogAsSingleton(ConfirmDialog.newInstance(onLaunch = { dialog, binding ->
                    with(binding) {
                        dialog.isCancelable = false
                        tvMessage.text = getString(R.string.message_inst_tts_data_ru)
                        ivIcon.setImageResource(R.drawable.ic_warning)
                        btnOk.apply {
                            text = getString(R.string.btn_text_setup)
                            setOnClickListener {
                                val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                try {
                                    startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    e.printStackTrace()
                                }
                                dialog.dismiss()
                            }
                        }
                        btnCancel.apply {
                            text = getString(R.string.btn_text_continue)
                            setOnClickListener {
                                speechChecked = true
                                dialog.dismiss()
                            }
                        }
                    }
                }), ConfirmDialog.TAG)
            }
        })

        startTimer(15000, onFinish = {
            authChecked = true
        })

        lifecycleScope.launch {
            while (!speechChecked || !authChecked) {
                delay(500)
                if (authChecked && speechChecked) {
                    this@SplashActivity.isUserRegistered(
                        onYes = {
                            appOpenAd?.show(this@SplashActivity)?: run {
                                startMainActivity()
                            }
                        },
                        onNotRegistered = {
                            startMainActivity()
                        }
                    )
                }
            }
        }

        openAdVM.resultAdData.observe(this) { result ->
            result.onSuccess { data: AdData ->
                setResult(NEED_UPDATE_USER_REWARD, Intent().apply {
                    putExtra("", data.toString())
                })
            }
            result.onFailure {
                startMainActivity()
            }
        }

    }

    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        this@SplashActivity.finish()
    }

}