@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.ads.ext

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.PopupLayoutBinding
import com.myapp.lexicon.databinding.PopupRewardPerAdBinding
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
        isFocusable = false

        setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_popup_dialog, null))
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

fun View.showUserRewardPerAdPopup(message: String) {
    val layoutInflater =
        this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val binding = PopupRewardPerAdBinding.inflate(layoutInflater)
    val popUp = PopupWindow(this.context)
    popUp.apply {
        contentView = binding.root
        width = LinearLayout.LayoutParams.WRAP_CONTENT
        height = LinearLayout.LayoutParams.WRAP_CONTENT
        isFocusable = false

        setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.bg_round_gold, null))
        animationStyle = R.style.scaleDownAndYtoUpAnimation

        showAsDropDown(this@showUserRewardPerAdPopup)
        val text = "+$message"
        binding.tvRewardValue.text = text
    }
    val duration = TimeUnit.SECONDS.toMillis(5)
    object : CountDownTimer(duration, duration) {
        override fun onTick(p0: Long) {}

        override fun onFinish() {
            popUp.dismiss()
        }
    }.start()
}

fun ViewGroup.showUserRewardAnimatedly(
    reward: String,
    coordinates: Pair<Int, Int>
) {

    val enterAnimDuration: Long = 1000
    val pauseMiddleDuration: Long = 3000
    val exitAnimDuration: Long = 200

    val coinLayout = this@showUserRewardAnimatedly.findViewById<ConstraintLayout>(R.id.layoutCoin)
    if (coinLayout != null) {
        this@showUserRewardAnimatedly.removeView(coinLayout)
    }

    val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val binding = PopupRewardPerAdBinding.inflate(layoutInflater)
    val text = "+$reward"
    binding.tvRewardValue.text = text

    val coinView = binding.root.apply {
        x = this@showUserRewardAnimatedly.width + 10f
        y = this@showUserRewardAnimatedly.height + 10f
        scaleX = 0f
        scaleY = 0f
    }
    this.addView(coinView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

    ValueAnimator.ofFloat(coinView.x, coordinates.first.toFloat() - coinView.width).apply {
        duration = enterAnimDuration
        addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                coinView.translationX = p0.animatedValue.toString().toFloat()
            }
        })
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                coinView.animate().apply {
                    duration = enterAnimDuration
                }.scaleX(1f)

                coinView.animate().apply {
                    duration = enterAnimDuration
                }.scaleY(1f)
            }

            override fun onAnimationEnd(p0: Animator) {
                coinView.animate().apply {
                    duration = exitAnimDuration
                    startDelay = pauseMiddleDuration
                }.scaleX(0f)

                coinView.animate().apply {
                    duration = exitAnimDuration
                    startDelay = pauseMiddleDuration
                }.scaleY(0f)
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })
    }.start()

    ValueAnimator.ofFloat(coinView.y, coordinates.second.toFloat()).apply {
        duration = enterAnimDuration
        addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                coinView.translationY = p0.animatedValue.toString().toFloat()
            }
        })
    }.start()

}






















