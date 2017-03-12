package com.myapp.lexicon.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.myapp.lexicon.R;

import java.util.Date;
import java.util.Random;

/**
 * Created by Ренат on 11.01.2017.
 */

public class BackgroundAnim extends AppCompatActivity
{
    ViewFlipper flipper;
    public static int displayedChild = 0;
    Context context;
    static int index = 0;
    Random randomGen;
    public BackgroundAnim(Context context, ViewFlipper flipper)
    {
        this.context = context;
        this.flipper = flipper;
    }

    public int startAnimByRandom()
    {
        if (index >= 0)
        {
            randomGen = new Random(new Date().getTime());
            index = randomGen.nextInt(flipper.getChildCount());
        }
        return startAnimByIndex(index);
    }

    public int startAnimByIndex(int index)
    {
        if (index < 0)
        {
            flipper.setDisplayedChild(displayedChild);
        } else
        {
            flipper.setDisplayedChild(index);
        }

        Animation animationFlipIn = AnimationUtils.loadAnimation(context, R.anim.flipin);

        Animation animationFlipOut = AnimationUtils.loadAnimation(context,R.anim.flipout);

        flipper.setInAnimation(animationFlipIn);
        flipper.setOutAnimation(animationFlipOut);

        flipper.startFlipping();
        BackgroundAnim.index = -1;
        return -1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        displayedChild = flipper.getDisplayedChild();
    }

    @Override
    protected void onDestroy()
    {
        flipper.stopFlipping();
        displayedChild = 0;
        index = 0;
    }
}
