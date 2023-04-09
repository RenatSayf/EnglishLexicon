package com.myapp.lexicon.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class DbMigrationViewModel @Inject constructor(app: Application, private val appDB: AppDB, private val appSettings: AppSettings) : AndroidViewModel(app)
{
    private val composite = CompositeDisposable()

    private fun migrateToWordsTable()
    {
        composite.add(
            appDB.getTableListAsync()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe({ list ->
                    list.forEach { dictName ->

                        val subscribe = appDB.copyEntriesFromOtherTableAsync(dictName)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ entries ->

                                val words = mutableListOf<Word>()
                                entries.forEach { entry ->
                                    val word = Word(
                                        0,
                                        dictName,
                                        entry.english,
                                        entry.translate,
                                        entry.countRepeat.toInt()
                                    )
                                    words.add(word)
                                }

                                if (words.isNotEmpty()) {
                                    val subscribe = appDB.insertIntoWordsTable(words)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ list ->
                                            println("*********************** Вставлено ${list.size} новых слов ************************")
                                            appSettings.setNotRequireDbMigration()
                                        }, { e ->
                                            e.printStackTrace()
                                            println("*********************** ${e.message} ************************")
                                        }, {
                                            println("*********************** Вставка завершена ************************")
                                        })
                                    composite.add(subscribe)
                                }


                            }, { t ->
                                t.printStackTrace()
                                println("*********************** ${t.message} ************************")
                            }, {
                                println("*********************** Миграция завершена ************************")
                            })
                        composite.add(subscribe)
                    }
                }, { e ->
                    e.printStackTrace()
                })
        )

    }

    init
    {
        val requireDbMigration = appSettings.isRequireDbMigration
        if (requireDbMigration)
        {
            migrateToWordsTable()
        }
    }

    override fun onCleared()
    {
        composite.apply {
            dispose()
            clear()
        }
        super.onCleared()
    }
}