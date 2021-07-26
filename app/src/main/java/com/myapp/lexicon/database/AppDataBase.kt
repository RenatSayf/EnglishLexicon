package com.myapp.lexicon.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myapp.lexicon.R


private const val DB_VERSION = 1


@Database(entities = [Word::class], version = DB_VERSION, exportSchema = false)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun appDao(): AppDao

    companion object
    {
        fun buildDataBase(context: Context): AppDataBase
        {
            val dbName = context.getString(R.string.data_base_name)
            return Room.databaseBuilder(context, AppDataBase::class.java, dbName).apply {
                createFromAsset("databases/$dbName")
                allowMainThreadQueries()
                addMigrations(getMigration(context, build()))
            }.build()
        }

        private fun getMigration(context: Context, appDB: AppDataBase): Migration
        {
            return object : Migration(DB_VERSION - 1, DB_VERSION)
            {
                override fun migrate(database: SupportSQLiteDatabase)
                {
                    println("***************************** migrate to $DB_VERSION started *******************************")
                    //val db = AppDB(DatabaseHelper(context), appDB.appDao())
                    //db.migrateToWordsTable()
                }
            }
        }

    }

}