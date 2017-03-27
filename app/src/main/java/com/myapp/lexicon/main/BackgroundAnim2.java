package com.myapp.lexicon.main;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Background animation class
 */

public class BackgroundAnim2 extends AppCompatActivity
{
    private static RandomNumberGenerator generator;

    private ImageView imageView1, imageView2;
    private Timer timer;
    private ArrayList<Integer> imagesArrayList;
    private ViewPropertyAnimator imageView1Animator, imageView2Animator;
    private long duration = 4000;
    private static int index = 0;
    private static int index2 = 1;
    private static float alpha1 = 1f;
    private static float alpha2 = 0f;

    public BackgroundAnim2(ImageView imageView1, ImageView imageView2)
    {
        imagesArrayList = new ArrayList<>();
        imagesArrayList.add(R.drawable.img_uk);
        imagesArrayList.add(R.drawable.img_uk2);
        imagesArrayList.add(R.drawable.img_uk5);
        imagesArrayList.add(R.drawable.img_uk4);
        imagesArrayList.add(R.drawable.img_uk6);
        imagesArrayList.add(R.drawable.img_uk7);
        imagesArrayList.add(R.drawable.img_uk8);
        imagesArrayList.add(R.drawable.img_usa2);
        imagesArrayList.add(R.drawable.img_usa3);
        imagesArrayList.add(R.drawable.img_usa4);

        this.imageView1 = imageView1;
        imageView1.setAlpha(alpha1);
        this.imageView2 = imageView2;
        imageView2.setAlpha(alpha2);
        timer = new Timer();
        if (generator == null)
        {
            generator = new RandomNumberGenerator(imagesArrayList.size(), (int) new Date().getTime());
            index = getRandomNumber(imagesArrayList.size());
            index2 = getRandomNumber(imagesArrayList.size());
        }
        imageView1.setImageResource(imagesArrayList.get(index));

        imageView2.setImageResource(imagesArrayList.get(index2));
    }

    public void startAnimBackground()
    {
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (imageView1.getAlpha() == 1 && imageView2.getAlpha() == 0)
                        {

                            imageView1Animator = imageView1.animate().alpha(0).setDuration(duration);
                            animationListener(imageView1Animator);

                            imageView2Animator = imageView2.animate().alpha(1).setDuration(duration);
                            animationListener(imageView2Animator);

                        }
                        if (imageView1.getAlpha() == 0 && imageView2.getAlpha() == 1)
                        {
                            imageView1Animator = imageView1.animate().alpha(1).setDuration(duration);
                            animationListener(imageView1Animator);

                            imageView2Animator = imageView2.animate().alpha(0).setDuration(duration);
                            animationListener(imageView2Animator);
                        }

                    }
                });
            }
        };
        long timerPeriod = 10000;
        long timerStart = 10000;
        timer.schedule(timerTask, timerStart, timerPeriod);
    }

    private void animationListener(ViewPropertyAnimator animator)
    {
       animator.setListener(new Animator.AnimatorListener()
       {
           @Override
           public void onAnimationStart(Animator animation)
           {

           }

           @Override
           public void onAnimationEnd(Animator animation)
           {
               if (imageView1.getAlpha() == 0f && imageView2.getAlpha() == 1f)
               {
                   index = getRandomNumber(imagesArrayList.size());
                   clearImageView(imageView1);
                   imageView1.setImageBitmap(null);
                   System.gc();
                   imageView1.setImageResource(imagesArrayList.get(index));
               }

               if (imageView2.getAlpha() == 0f && imageView1.getAlpha() == 1f)
               {
                   index2 = getRandomNumber(imagesArrayList.size());
                   clearImageView(imageView2);
                   imageView2.setImageBitmap(null);
                   System.gc();
                   imageView2.setImageResource(imagesArrayList.get(index2));
               }
               alpha1 = imageView1.getAlpha();
               alpha2 = imageView2.getAlpha();
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

    private static int getRandomNumber(int range)
    {
        int number = generator.generate();
        if (number < 0)
        {
            generator = new RandomNumberGenerator(range, (int) new Date().getTime());
            number = generator.generate();
        }
        return number;
    }

    public static void clearImageView(ImageView imageView)
    {
        if (imageView.getDrawable() instanceof BitmapDrawable)
        {
            return;
        }
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        if (bitmap != null && !bitmap.isRecycled())
        {
            bitmap.recycle();
        }
        imageView.setImageDrawable(null);
        imageView.setImageBitmap(null);
    }

    protected void on_Destroy()
    {

    }
}
