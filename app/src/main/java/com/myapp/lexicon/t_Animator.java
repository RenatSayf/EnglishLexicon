package com.myapp.lexicon;

import android.animation.Animator;
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
    public static IButtonsToDownEnd iButtonsToDownEnd;
    public static IButtonToTopListener iButtonToTopListener;

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

    public interface IButtonsToDownEnd
    {
        void buttonsToDownEnd(int result);
    }

    public void registrationButtonsToDownEnd(IButtonsToDownEnd end)
    {
        iButtonsToDownEnd = end;
    }

    public interface IButtonToTopListener
    {
        void buttonToTopListener(int result);
    }

    public void setButtonToTopListener(IButtonToTopListener end)
    {
        iButtonToTopListener = end;
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

    public void buttonsToDown(int index)
    {
        for (int i=index-1; i >= 0; i--)
        {
            animToDown = buttons[i].animate()
                    .translationYBy(buttons[index].getHeight())
                    .setDuration(duration)
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            if (i == 0)
            {
                buttonsToDown_Listener(animToDown);
            }
        }
    }

    private void buttonsToDown_Listener(ViewPropertyAnimator animToDown)
    {
        animToDown.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (iButtonsToDownEnd != null)
                {
                    iButtonsToDownEnd.buttonsToDownEnd(1);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
                if (iButtonsToDownEnd != null)
                {
                    iButtonsToDownEnd.buttonsToDownEnd(-1);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }

    public void buttonToTop(int index)
    {
        animToTop = buttons[index].animate()
                .translationYBy(-buttons[index].getHeight() * index)
                .setDuration(10)
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        if (iButtonToTopListener != null)
                        {
                            iButtonToTopListener.buttonToTopListener(1);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {
                        if (iButtonToTopListener != null)
                        {
                            iButtonToTopListener.buttonToTopListener(-1);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
    }

    public static IButtonToLeftListener iButtonToLeftListener;
    public void setButtonToLeftListener(IButtonToLeftListener listener)
    {
        iButtonToLeftListener = listener;
    }

    public interface IButtonToLeftListener
    {
        void buttonToLeftListener(int result);
    }

    public void buttonToLeft(int index)
    {
        buttons[index].animate().x(delta).setDuration(duration)
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

                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
    }

    public void textViewToRight()
    {
        textView.animate().translationX(0)
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
                        if (iButtonToLeftListener != null)
                        {
                            iButtonToLeftListener.buttonToLeftListener(1);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {
                        if (iButtonToLeftListener != null)
                        {
                            iButtonToLeftListener.buttonToLeftListener(1);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
    }
}
