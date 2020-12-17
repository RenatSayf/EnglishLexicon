package com.myapp.lexicon.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myapp.lexicon.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream


private const val DB_VERSION = 1

@Database(entities = [Word::class], version = DB_VERSION, exportSchema = false)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun appDao(): AppDao

    companion object
    {
        private lateinit var mContext: Context
        private var instance : AppDataBase? = null

        fun getInstance(context: Context) : AppDataBase
        {
            mContext = context
            return instance ?: synchronized(this){
                instance ?: buildDataBase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDataBase(context: Context) : AppDataBase
        {
            val dbName = context.getString(R.string.data_base_name)
            return Room.databaseBuilder(context, AppDataBase::class.java, dbName)
                    .addMigrations(MIGRATION)
                    .createFromAsset("databases/$dbName")
                    .allowMainThreadQueries()
                    .build()
        }

        private val MIGRATION = object : Migration(DB_VERSION - 1, DB_VERSION){
            override fun migrate(database: SupportSQLiteDatabase)
            {
                if (database.needUpgrade(DB_VERSION))
                {
//                    println("***************************** migrate to $DB_VERSION started *******************************")
//                    val db = AppDB(DatabaseHelper(mContext))
//                    val subscribe = db.getTableListAsync()
//                            .observeOn(Schedulers.computation())
//                            .subscribeOn(AndroidSchedulers.mainThread())
//                            .subscribe { list ->
//                                list.forEach { dictName ->
//                                    db.copyEntriesFromOtherTableAsync(dictName)
//                                            .observeOn(Schedulers.computation())
//                                            .subscribeOn(AndroidSchedulers.mainThread())
//                                            .subscribe({ res ->
//                                               println("******************************** ${this::class.java.canonicalName} res = $res **********************************")
//                                            }, { t ->
//                                                t.printStackTrace()
//                                            })
//                                }
//                            }
                }
            }

        }
    }
}