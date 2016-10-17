package com.myapp.lexicon;

import android.animation.Animator;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private int delta = 50;

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
                        //buttonToTop(tempButton);
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

    private Button tempButton;
    public void buttonToRight(LinearLayout layout, float x, float y)
    {
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            Button button = (Button) layout.getChildAt(i);
            if (y == button.getY() && x == button.getX())
            {
                tempButton = button;
                animToRight = button.animate().translationXBy((button.getWidth() + delta))
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
                                //buttonToTop(tempButton);
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
                break;
            }
        }
    }

    public void buttonsToDown(LinearLayout layout, float x, float y)
    {
        ViewPropertyAnimator animToDown = null;
        boolean isListener = false;
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            Button button = (Button) layout.getChildAt(i);
            float X = button.getX();
            float Y = button.getY();
            float height = button.getHeight();
            if (Y < y && x == X)
            {
                animToDown = button.animate()
                        .translationYBy(button.getHeight())
                        .setDuration(duration)
                        .setInterpolator(new AccelerateDecelerateInterpolator());
                if (!isListener)
                {
                    isListener = true;
                    buttonsToDown_Listener(animToDown);
                }
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
//                if (iButtonsToDownEnd != null)
//                {
//                    iButtonsToDownEnd.buttonsToDownEnd(1);
//                }
                buttonToTop(tempButton);
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

    public void buttonToTop(Button button)
    {
        animToTop = button.animate()
                .translationYBy(-button.getY())
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
                        //buttonToLeft(tempButton);
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

    public void buttonToLeft(Button button)
    {
        button.animate().translationXBy(-button.getWidth()-delta).setDuration(duration)
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
                        buttonToLeft(tempButton);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        if (iButtonToLeftListener != null)
                        {
                            iButtonToLeftListener.buttonToLeftListener(1);
                        }
                        //buttonToLeft(tempButton);
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
