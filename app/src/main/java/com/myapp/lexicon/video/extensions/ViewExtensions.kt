@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout


fun LinearLayoutCompat.changeHeightAnimatedly(
    value: Int = this.height,
    onEnd: (isVisible: Boolean) -> Unit = {}
) {
    val layoutParams = this@changeHeightAnimatedly.layoutParams as ConstraintLayout.LayoutParams
    val paramToAnimate = layoutParams.height
    ValueAnimator.ofInt((paramToAnimate), value).apply {
        addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animator: ValueAnimator) {
                layoutParams.height = animator.animatedValue as Int
                this@changeHeightAnimatedly.layoutParams = layoutParams
            }
        })
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                val animValue = (p0 as ValueAnimator).animatedValue as Int
                //"*********** animValue = $animValue ****************".logIfDebug()
                if (animValue == this@changeHeightAnimatedly.height) {
                    onEnd.invoke(true)
                }
                else {
                    onEnd.invoke(false)
                }
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}

        })
    }.setDuration(200).start()
}