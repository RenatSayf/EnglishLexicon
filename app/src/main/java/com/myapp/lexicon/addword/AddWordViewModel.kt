package com.myapp.lexicon.addword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddWordViewModel @Inject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{

    private val _spinnerSelectedIndex = MutableLiveData<Int>()
    fun spinnerSelectedIndex(): LiveData<Int>
    {
        return _spinnerSelectedIndex
    }

    fun setSelected(index: Int)
    {
        _spinnerSelectedIndex.value = index
    }

    private var _insertedWord = MutableLiveData<Pair<Word?, Throwable?>>(Pair(null, null))
    var insertedWord: LiveData<Pair<Word?, Throwable?>> = _insertedWord

    fun insertWord(word: Word)
    {
        viewModelScope.launch {
            try {
                val ids = repository.insertWordListAsync(listOf(word)).await()
                try {
                    val id = ids.first()
                    _insertedWord.value = Pair(word.apply { this._id = id.toInt() }, null)
                } catch (e: NoSuchElementException) {
                    _insertedWord.value = Pair(null, Throwable("***** Database error ****"))
                }
            } catch (e: Exception) {
                _insertedWord.value = Pair(null, e)
            }
        }
    }

}