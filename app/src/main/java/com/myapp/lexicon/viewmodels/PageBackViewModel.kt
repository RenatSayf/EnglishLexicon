package com.myapp.lexicon.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.IndexOutOfBoundsException
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class PageBackViewModel @Inject constructor(app: Application) : AndroidViewModel(app)
{
    private val imageArray = arrayOf(
        R.drawable.img_uk4,
        R.drawable.img_uk4,
        R.drawable.img_uk6,
        R.drawable.img_uk7,
        R.drawable.img_uk8,
        R.drawable.img_uk10,
        R.drawable.img_uk11,
        R.drawable.img_uk12,
        R.drawable.img_uk15,
        R.drawable.img_uk17,
        R.drawable.img_uk18,
        R.drawable.img_uk19,
        R.drawable.img_uk20,
        R.drawable.img_uk21,
        R.drawable.img_uk22,
        R.drawable.img_uk23,
        R.drawable.img_uk27
    )

    private var _imageBack = MutableLiveData(R.drawable.img_uk22).apply {
        val range = imageArray.size
        val randomIndex = Random.nextInt(range)
        value = try
        {
            imageArray[randomIndex]
        } catch (e: IndexOutOfBoundsException)
        {
            R.drawable.img_uk22
        }
    }
    var imageBack: LiveData<Int> = _imageBack
}