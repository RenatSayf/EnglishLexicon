package com.myapp.lexicon.addword

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import kotlinx.coroutines.launch


class AddWordViewModel(private val repository: DataRepositoryImpl) : ViewModel()
{
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == AddWordViewModel::class.java)
            return AddWordViewModel(
                repository = DataRepositoryImpl(AppDataBase.getDbInstance(context).appDao())
            ) as T
        }
    }

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