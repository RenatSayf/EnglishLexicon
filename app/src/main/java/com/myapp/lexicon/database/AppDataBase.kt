package com.myapp.lexicon.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myapp.lexicon.R
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


private const val DB_VERSION = 1


@Database(entities = [Word::class], version = DB_VERSION, exportSchema = false)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun appDao(): AppDao



    companion object
    {
        private var mContext: Context? = null
        private var mDB: AppDataBase? = null
//        private var instance : AppDataBase? = null
//
//        fun getInstance(context: Context) : AppDataBase
//        {
//            mContext = context
//            return instance ?: synchronized(this){
//                instance ?: buildDataBase(context).also {
//                    instance = it
//                }
//            }
//        }

        fun buildDataBase(context: Context) : AppDataBase
        {
            mContext = context
            val dbName = context.getString(R.string.data_base_name)
            mDB =  Room.databaseBuilder(context, AppDataBase::class.java, dbName)
                    .addMigrations(MIGRATION)
                    .createFromAsset("databases/$dbName")
                    .allowMainThreadQueries()
                    .build()
            return mDB as AppDataBase
        }

        private val MIGRATION = object : Migration(DB_VERSION - 1, DB_VERSION)
        {
            override fun migrate(database: SupportSQLiteDatabase)
            {
                if (database.needUpgrade(DB_VERSION))
                {
                    println("***************************** migrate to $DB_VERSION started *******************************")
                    mDB?.let { room ->
                        val db = AppDB(DatabaseHelper(mContext), room.appDao())
                        db.migrateToWordsTable()
//                        db.getTableListAsync()
//                            .observeOn(Schedulers.io())
//                            .subscribeOn(AndroidSchedulers.mainThread())
//                            .subscribe { list ->
//                                list.forEach { dictName ->
//                                    db.copyEntriesFromOtherTableAsync(dictName)
//                                        .observeOn(Schedulers.io())
//                                        .subscribeOn(AndroidSchedulers.mainThread())
//                                        .subscribe({ entries ->
//
//                                            val words = mutableListOf<Word>()
//                                            entries.forEach { entry ->
//                                                val word = Word(
//                                                    1,
//                                                    dictName,
//                                                    entry.english,
//                                                    entry.translate,
//                                                    entry.countRepeat.toInt()
//                                                )
//                                                words.add(word)
//                                            }
//
//                                            db.insertIntoWordsTable(words)
//                                                .observeOn(Schedulers.io())
//                                                .subscribeOn(AndroidSchedulers.mainThread())
//                                                .subscribe({ list ->
//
//                                                }, { e ->
//                                                    e.printStackTrace()
//                                                }, {
//                                                    mContext = null
//                                                    mDB = null
//                                                })
//
//                                        }, { t ->
//                                            t.printStackTrace()
//                                        }, {
//
//                                        })
//                                }
//                            }
                    }
                }
            }
        }
    }

}