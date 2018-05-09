package com.myapp.lexicon.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.RandomNumberGenerator;

import java.util.Date;

/**
 * TODO: AdapterViewFlipper: 5. адаптер для AdapterViewFlipper
 */

public class FlipperAdapter extends BaseAdapter
{
    private Context context;
    private int[] images;
    private RandomNumberGenerator generator; // генератор случайных чисел из диапазона без повторяющихся значений

    FlipperAdapter(Context context, int[] images)
    {
        this.context = context;
        this.images = images;

        // инициализация генератора
        generator = new RandomNumberGenerator(images.length, (int) new Date().getTime());
    }

    @Override
    public int getCount()
    {
        return images.length;
    }

    @Override
    public Object getItem(int i)
    {
        return context.getResources().getDrawable(images[i]);
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        View adapterView = view;
        if (adapterView == null)
        {
            // предварительно создать макет адаптера в директории res/layout
            adapterView = LayoutInflater.from(context).inflate(R.layout.a_images_layout, viewGroup, false);
        }

        int index = generator.generate(); // генерация случайного индекса
        if (index < 0) // если все числа в диапазоне кончились то
        {
            // инициализация нового генератора
            generator = new RandomNumberGenerator(images.length, (int) new Date().getTime());
            index = generator.generate();
        }

        ImageView imageView1 = adapterView.findViewById(R.id.image_background_1);
        imageView1.setImageResource(images[index]);

        return adapterView;
    }
}
