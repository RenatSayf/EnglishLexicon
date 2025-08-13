package com.myapp.lexicon.splash

import admost.sdk.base.AdMost
import admost.sdk.base.AdMostConfiguration
import admost.sdk.listener.AdMostInitListener
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.appodeal.ads.Appodeal
import com.appodeal.ads.utils.Log
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.ext.initAppodealAd
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.common.KEY_APP_STORE_LINK
import com.myapp.lexicon.common.MESSAGE_TO_USER
import com.myapp.lexicon.databinding.ALayoutSplashScreenBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.showDialogAsSingleton
import com.myapp.lexicon.helpers.startTimer
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.Speaker
import com.myapp.lexicon.settings.checkOnStartSpeech
import com.myapp.lexicon.settings.goToAppStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        const val KEY_AD_DATA = "KEY_AD_DATA_258741359"
    }

    private lateinit var binding: ALayoutSplashScreenBinding
    private var speaker: Speaker? = null
    private var speechChecked = false
    private var adsInitChecked = false

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

        val configuration = AdMostConfiguration.Builder(this, "dc5ba8a0-9729-43ab-9360-81149835d5ab").apply {
                setSubjectToCCPA(false)
                setSubjectToGDPR(false)
                setUserConsent(true)
                showUIWarningsForDebuggableBuild(BuildConfig.DEBUG)
            }.build()

        AdMost.getInstance().init(configuration, object : AdMostInitListener {
            override fun onInitCompleted() {
                "******************* AdMost init SUCCESSFUL **********************".logIfDebug()
            }

            override fun onInitFailed(p0: Int) {
                "******************* AdMost init error: $p0 **********************".logIfDebug()
            }

        })

        if (BuildConfig.DEBUG) {
            Appodeal.setLogLevel(logLevel = Log.LogLevel.verbose)
        }
        else {
            Appodeal.setLogLevel(logLevel = Log.LogLevel.none)
        }
        Appodeal.setTesting(BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name)

        val adTypes = listOf(
            Appodeal.INTERSTITIAL,
            Appodeal.NATIVE,
            Appodeal.REWARDED_VIDEO,
            Appodeal.BANNER_VIEW
        )

        adTypes.forEachIndexed(action = { index, type ->
            this@SplashActivity.initAppodealAd(
                adType = type,
                onCompleted = {
                    adsInitChecked = true
                }
            )
        })

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
                                    data = getString(R.string.url_google_tts).toUri()
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

        TimeUnit.SECONDS.toMillis(30).startTimer(
            onFinish = {
                speechChecked = true
                adsInitChecked = true
            }
        )

        lifecycleScope.launch {
            while (!speechChecked || !adsInitChecked) {
                delay(500)
                if (speechChecked && adsInitChecked) {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    this@SplashActivity.finish()
                }
            }
        }
    }

}