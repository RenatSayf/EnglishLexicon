package com.myapp.lexicon.ads.ext

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.PopupLayoutBinding
import java.util.concurrent.TimeUnit

fun View.showAdPopup(
    onClick: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    val layoutInflater =
        this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val binding = PopupLayoutBinding.inflate(layoutInflater)
    val popUp = PopupWindow(this@showAdPopup.context)
    popUp.apply {
        contentView = binding.root
        width = LinearLayout.LayoutParams.WRAP_CONTENT
        height = LinearLayout.LayoutParams.WRAP_CONTENT
        isFocusable = true

        //setBackgroundDrawable(ColorDrawable())
        animationStyle = R.style.popupWindowAnimation
        showAsDropDown(this@showAdPopup)

        binding.layoutRoot.setOnClickListener {
            onClick.invoke()
            dismiss()
        }
    }
    val duration = TimeUnit.SECONDS.toMillis(10)
    object : CountDownTimer(duration, duration) {
        override fun onTick(p0: Long) {}

        override fun onFinish() {
            popUp.dismiss()
            onDismissed.invoke()
        }
    }.start()
}