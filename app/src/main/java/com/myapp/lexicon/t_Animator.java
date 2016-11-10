package com.myapp.lexicon;

import android.animation.Animator;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Ренат on 12.10.2016.
 */

public class t_Animator
{
    public  static t_Animator instance = new t_Animator();

    public ITextViewToLeftListener iTextViewToLeftListener;
    public ITextViewToRightListener iTextViewToRightListener;

    private TextView textView;
    private RelativeLayout layout;
    private long duration = 1000;
    private int delta = 60;

    public t_Animator()
    {

    }

    public static t_Animator getInstance()
    {
        return instance;
    }

    public void setLayout(RelativeLayout layout, TextView textView)
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
                        if (iTextViewToLeftListener != null)
                        {
                            iTextViewToLeftListener.textViewToLeftListener(1, textView, button);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {
                        if (iTextViewToLeftListener != null)
                        {
                            iTextViewToLeftListener.textViewToLeftListener(-1, textView, button);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
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

    public void buttonToRight(RelativeLayout layout, int buttonId)
    {
        ViewPropertyAnimator animToRight;
        setButtonId(buttonId);

        button = (Button) layout.findViewById(getButtonId());
        if (button != null)
        {
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
                            int id = getButtonId();
                            buttonToTop();
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
    }

    public int getMinMarginTop()
    {
        int topMargin = 0;
        try
        {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout.getChildAt(0).getLayoutParams();
            topMargin = layoutParams.topMargin;
            for (int i = 0; i < layout.getChildCount(); i++)
            {
                layoutParams = (RelativeLayout.LayoutParams) layout.getChildAt(i).getLayoutParams();
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
                        .setDuration(duration)
                        .setInterpolator(new AccelerateDecelerateInterpolator());
                if (!isListener)
                {
                    isListener = true;
                    animToDown.setListener(new Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationStart(Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            textViewToRight();
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

    private void buttonToLeft()
    {
        final Button button = (Button) layout.findViewById(getButtonId());
        if (button == null) return;
        button.animate().translationXBy(-button.getWidth()-delta).setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void buttonToLeft(int buttonId)
    {
        Button button = (Button) layout.findViewById(buttonId);
        if (button == null) return;
        button.animate().translationXBy(-button.getWidth() - delta)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                        textViewToRight();
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
        TextView _textView = this.textView;
        _textView.animate().translationX(0)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                        buttonToLeft();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        if (iTextViewToRightListener != null)
                        {
                            iTextViewToRightListener.textViewToRightListener(1, textView);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {
                        if (iTextViewToRightListener != null)
                        {
                            iTextViewToRightListener.textViewToRightListener(-1, textView);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
    }
}
