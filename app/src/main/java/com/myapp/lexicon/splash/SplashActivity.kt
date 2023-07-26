@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.R
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.billing.UserPurchases
import com.myapp.lexicon.databinding.ALayoutSplashScreenBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.showDialogAsSingleton
import com.myapp.lexicon.helpers.startTimer
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.Speaker
import com.myapp.lexicon.settings.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ALayoutSplashScreenBinding
    private var speaker: Speaker? = null

    private val authVM: AuthViewModel by viewModels()

    private var authChecked = false
    private var cloudChecked = false
    private var speechChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ALayoutSplashScreenBinding.inflate(layoutInflater, CoordinatorLayout(this), false)
        setContentView(binding.root)

        applicationContext.adsIsEnabled = true

        applicationContext.getAuthDataFromPref(
            onNotRegistered = {
                authChecked = true
            },
            onSuccess = { id, email, password ->
                authVM.signInWithEmailAndPassword(email, password)
            }
        )

        authVM.state.observe(this) { state ->
            state.onSignIn { user ->
                authChecked = true
            }
            state.onFailure {
                authChecked = true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        this.checkBuildConfig(
            onInit = {
                this.setCloudSetting(null)
            },
            onChangeToTest = { mode ->
                this.setCloudSetting("")
            },
            onChangeToNormal = { mode ->
                this.setCloudSetting(null)
            }
        )

        this.checkPurchasesTokens(listener = object : PurchasesTokenListener {
            override fun onInit() {
                UserPurchases(this@SplashActivity, object : UserPurchases.Listener {
                    override fun onExistsAdsToken(token: String) {}

                    override fun onEmptyAdsToken() {}

                    override fun onExistsCloudToken(token: String) {
                        this@SplashActivity.setCloudSetting(token)
                        cloudChecked = true
                    }

                    override fun onEmptyCloudToken() {
                        this@SplashActivity.setCloudSetting("")
                        cloudChecked = true
                    }
                })
            }

            override fun onCheckComplete() {
                cloudChecked = true
            }
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
        })

        startTimer(30000, onFinish = {
            cloudChecked = true
            authChecked = true
        })

        lifecycleScope.launch {
            while (!cloudChecked || !speechChecked || !authChecked) {
                delay(500)
                if (authChecked && cloudChecked && speechChecked) {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    this@SplashActivity.finish()
                }
            }
        }

    }

    override fun onDestroy() {

        authChecked
        super.onDestroy()
    }
}