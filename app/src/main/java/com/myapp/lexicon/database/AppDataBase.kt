package com.myapp.lexicon.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.getCRC32CheckSum
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.saveInitDbCheckSum


private const val DB_VERSION = 1


@Database(entities = [Word::class], version = DB_VERSION, exportSchema = true)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun appDao(): AppDao

    companion object
    {
        var dataBase: AppDataBase? = null

        fun buildDataBase(context: Context): AppDataBase
        {
            val dbName = context.getString(R.string.data_base_name)
            dataBase = Room.databaseBuilder(context, AppDataBase::class.java, dbName).apply {
                createFromAsset("databases/$dbName")
                //addMigrations(getMigration())
            }.build().apply {
                val checkSum = context.assets.open("databases/$dbName").readBytes().getCRC32CheckSum()
                context.saveInitDbCheckSum(checkSum)
                if (BuildConfig.DEBUG) {
                    println("************** Database init check sum = $checkSum **********************")
                }
            }
            return dataBase as AppDataBase
        }

        private fun getMigration(): Migration
        {
            return object : Migration(DB_VERSION - 1, DB_VERSION)
            {
                override fun migrate(database: SupportSQLiteDatabase)
                {
                    //println("***************************** migrate to $DB_VERSION started *******************************")
                }
            }
        }

    }

}