package com.myapp.lexicon;

import android.animation.Animator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Ренат on 12.10.2016.
 */

public class t_Animator extends Animation
{
    public static ITextViewToleftEnd iTextViewToleftEnd;

    private Button[] buttons;
    private TextView textView;
    private ViewPropertyAnimator animToLeft, animToRight, animToTop, animToDown;
    private long duration = 1000;
    private int delta = 100;

    public t_Animator(Button[] buttons, TextView textView)
    {
        this.buttons = buttons;
        this.textView = textView;
    }

    public interface ITextViewToleftEnd
    {
        void textViewToLeftEnd(int result);
    }

    public void registrationITextViewToleftEnd(ITextViewToleftEnd end)
    {
        iTextViewToleftEnd = end;
    }

    public void textViewToLeft()
    {

        textView.animate().x(-(textView.getWidth() + delta))
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        if (iTextViewToleftEnd != null)
                        {
                            iTextViewToleftEnd.textViewToLeftEnd(1);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {
                        if (iTextViewToleftEnd != null)
                        {
                            iTextViewToleftEnd.textViewToLeftEnd(-1);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
    }

    public void buttonToRight(int index)
    {
        animToRight = buttons[index].animate().x((buttons[index].getWidth() + delta))
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator());
    }
}
