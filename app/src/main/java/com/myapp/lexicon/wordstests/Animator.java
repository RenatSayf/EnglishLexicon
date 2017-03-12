package com.myapp.lexicon.wordstests;

import android.util.DisplayMetrics;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ренат on 12.10.2016.
 */

public class Animator
{
    public  static Animator instance = new Animator();

    public ITextViewToLeftListener iTextViewToLeftListener;
    public ITextViewToRightListener iTextViewToRightListener;

    private TextView textView;
    private LinearLayout layout;
    private long duration = 1000;
    private int delta = 60;

    public Animator()
    {

    }

    public static Animator getInstance()
    {
        return instance;
    }

    public void setLayout(LinearLayout layout, TextView textView)
    {
        this.textView = textView;
        this.layout = layout;
    }

    public interface ITextViewToLeftListener
    {
        void textViewToLeftListener(int result, TextView textView, Button button);
    }

    public void setTextViewToLeftListener(ITextViewToLeftListener listener)
    {
        iTextViewToLeftListener = listener;
    }

    public interface ITextViewToRightListener
    {
        void textViewToRightListener(int result, TextView textView);
    }

    public void setTextViewToRightListener(ITextViewToRightListener listener)
    {
        iTextViewToRightListener = listener;
    }

    public void textViewToLeft(DisplayMetrics metrics)
    {
        textView.animate().x(-(metrics.widthPixels + delta))
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(0)
                .setListener(new android.animation.Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation)
                    {
                        if (iTextViewToLeftListener != null)
                        {
                            iTextViewToLeftListener.textViewToLeftListener(1, textView, button);
                        }
                    }

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation)
                    {
                        if (iTextViewToLeftListener != null)
                        {
                            iTextViewToLeftListener.textViewToLeftListener(-1, textView, button);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation)
                    {

                    }
                });
    }

    private Button button;
    private int buttonId;
    private int getButtonId()
    {
        return buttonId;
    }

    private void setButtonId(int buttonId)
    {
        this.buttonId = buttonId;
    }

    public void buttonToRight(LinearLayout layout, int buttonId, DisplayMetrics metrics)
    {
        ViewPropertyAnimator animToRight;
        setButtonId(buttonId);

        button = (Button) layout.findViewById(getButtonId());
        if (button != null)
        {
            animToRight = button.animate().translationXBy((button.getWidth()+delta))
                    .setDuration(duration)
                    .setStartDelay(0)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setListener(new android.animation.Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationStart(android.animation.Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationEnd(android.animation.Animator animation)
                        {
                            int id = getButtonId();
                            buttonToTop();
                        }

                        @Override
                        public void onAnimationCancel(android.animation.Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationRepeat(android.animation.Animator animation)
                        {

                        }
                    });
        }
    }

    private int getMinMarginTop()
    {
        int topMargin = 0;
        try
        {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getChildAt(0).getLayoutParams();
            topMargin = layoutParams.topMargin;
            for (int i = 0; i < layout.getChildCount(); i++)
            {
                layoutParams = (LinearLayout.LayoutParams) layout.getChildAt(i).getLayoutParams();
                if (layoutParams.topMargin < topMargin)
                {
                    topMargin = layoutParams.topMargin;
                }
            }
        } catch (Exception e)
        {
            return 0;
        }
        return topMargin;
    }

    public void buttonsToDown(float x, float y)
    {
        ViewPropertyAnimator animToDown;
        boolean isListener = false;
        int topMargin = getMinMarginTop();
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            Button button = (Button) layout.getChildAt(i);
            float X = button.getX();
            float Y = button.getY();
            if (Y < y && x == X && button.getId() != getButtonId())
            {
                animToDown = button.animate()
                        .translationYBy(button.getHeight()+topMargin)
                        .setDuration(300)
                        .setStartDelay(0)
                        .setInterpolator(new AccelerateInterpolator());
                if (!isListener)
                {
                    isListener = true;
                    animToDown.setListener(new android.animation.Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationStart(android.animation.Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationEnd(android.animation.Animator animation)
                        {
                            textViewToRight();
                        }

                        @Override
                        public void onAnimationCancel(android.animation.Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationRepeat(android.animation.Animator animation)
                        {

                        }
                    });
                }
            }
        }
    }

    private void buttonToTop()
    {
        final Button button = (Button) layout.findViewById(getButtonId());
        if (button == null) return;
        int topMargin = getMinMarginTop();
        button.animate().translationYBy(-button.getY()+topMargin)
                .setDuration(10)
                .setStartDelay(0)
                .setListener(new android.animation.Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation)
                    {

                    }
                });
    }

    private void buttonToLeft()
    {
        final Button button = (Button) layout.findViewById(getButtonId());
        if (button == null) return;
        button.animate().translationXBy(-button.getWidth()-delta)
                .setDuration(duration)
                .setStartDelay(0)
                .setInterpolator(new AnticipateOvershootInterpolator());
    }

    public void buttonToLeft(int buttonId)
    {
        Button button = (Button) layout.findViewById(buttonId);
        if (button == null) return;
        button.animate().translationXBy(-button.getWidth() - delta)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new android.animation.Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation)
                    {
                        textViewToRight();
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation)
                    {

                    }
                });
    }

    public void textViewToRight()
    {
        TextView _textView = this.textView;
        _textView.animate().translationX(0)
                .setDuration(duration)
                .setStartDelay(0)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .setListener(new android.animation.Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation)
                    {
                        buttonToLeft();
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation)
                    {
                        if (iTextViewToRightListener != null)
                        {
                            iTextViewToRightListener.textViewToRightListener(1, textView);
                        }
                    }

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation)
                    {
                        if (iTextViewToRightListener != null)
                        {
                            iTextViewToRightListener.textViewToRightListener(-1, textView);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation)
                    {

                    }
                });
    }
}
