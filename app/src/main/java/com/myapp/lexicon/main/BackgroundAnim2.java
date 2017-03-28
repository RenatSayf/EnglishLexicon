package com.myapp.lexicon.main;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.RandomNumberGenerator;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Background animation class
 */

public class BackgroundAnim2 extends AppCompatActivity
{
    private static RandomNumberGenerator generator;
    private static Drawable drawable1, drawable2;
    private ImageView imageView1, imageView2;
    private Timer timer;
    private int[] imagesId = new int[]
            {
                    R.drawable.img_uk,
                    R.drawable.img_uk2,
                    R.drawable.img_uk5,
                    R.drawable.img_uk4,
                    R.drawable.img_uk7,
                    R.drawable.img_usa4
            };
    private ViewPropertyAnimator imageView1Animator, imageView2Animator;
    private Handler handler;
    private long duration = 4000;
    private static int index = 0;
    private static int index2 = 1;

    public BackgroundAnim2(ImageView imageView1, ImageView imageView2)
    {
        this.imageView1 = imageView1;
        this.imageView2 = imageView2;
        timer = new Timer();
        if (generator == null)
        {
            generator = new RandomNumberGenerator(imagesId.length, (int) new Date().getTime());
            index = getRandomNumber(imagesId.length);
            index2 = getRandomNumber(imagesId.length);
        }

        if (drawable1 != null)
        {
            imageView1.setImageDrawable(drawable1);
            imageView2.setImageDrawable(drawable2);
            drawable1 = null;
            drawable2 = null;
        }
        else
        {
            imageView1.setImageResource(imagesId[index]);
            imageView2.setImageResource(imagesId[index2]);
        }

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
                   Thread thread = new Thread(new Runnable()
                   {
                       @Override
                       public void run()
                       {
                            handler.sendEmptyMessage(0);
                       }
                   });
                   handler = new Handler()
                   {
                       public void handleMessage(Message msg)
                       {
                           imageView1.setImageBitmap(null);
                           imageView1.destroyDrawingCache();
                           index = getRandomNumber(imagesId.length);
                           clearImageView(imageView1);
                           System.gc();
                           imageView1.setImageResource(imagesId[index]);
                       }
                   };
                   thread.start();
               }

               if (imageView1.getAlpha() == 1f && imageView2.getAlpha() == 0f)
               {
                   Thread thread = new Thread(new Runnable()
                   {
                       @Override
                       public void run()
                       {
                           handler.sendEmptyMessage(0);
                       }
                   });
                   handler = new Handler()
                   {
                       public void handleMessage(Message msg)
                       {
                           imageView2.setImageBitmap(null);
                           imageView2.destroyDrawingCache();
                           index2 = getRandomNumber(imagesId.length);
                           clearImageView(imageView2);
                           System.gc();
                           imageView2.setImageResource(imagesId[index2]);
                       }
                   };
                   thread.start();
               }
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

    public void saveState()
    {
        if (imageView1.getAlpha() > imageView2.getAlpha())
        {
            drawable1 = imageView1.getDrawable();
            drawable2 = imageView2.getDrawable();
        }
        else
        {
            drawable1 = imageView2.getDrawable();
            drawable2 = imageView1.getDrawable();
        }
    }
}
