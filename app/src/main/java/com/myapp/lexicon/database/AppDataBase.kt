@file:Suppress("RedundantSamConstructor")

package com.myapp.lexicon.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myapp.lexicon.R
import com.myapp.lexicon.database.models.WordToPlay
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.video.models.query.HistoryQuery
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import java.io.File


private const val DB_VERSION = 3


@Database(entities = [Word::class, WordToPlay::class, HistoryQuery::class], version = DB_VERSION, exportSchema = true)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun appDao(): AppDao

    companion object
    {
        @Volatile
        var dataBase: AppDataBase? = null
            private set

        @OptIn(InternalCoroutinesApi::class)
        fun getDbInstance(context: Context): AppDataBase {
            return if (dataBase == null) {
                dataBase = synchronized(this){
                    buildDataBase(context)
                }
                dataBase!!
            }
            else {
                dataBase!!
            }
        }

        private fun buildDataBase(context: Context): AppDataBase
        {
            val dbName = context.getString(R.string.data_base_name)

            dataBase = Room.databaseBuilder(context, AppDataBase::class.java, dbName).apply {

                addMigrations(getMigrationFrom1To2())
                addMigrations(getMigrationFrom2To3())
                allowMainThreadQueries()
                val dbFile = context.getDatabasePath(dbName)
                if (dbFile.exists()) {
                    createFromFile(dbFile)
                }
                else {
                    createFromAsset("databases/$dbName")
                }
            }.build()
            return dataBase!!
        }

        fun buildDataBaseFromFile(context: Context, dbFile: File): AppDataBase {
            val dbName = context.getString(R.string.data_base_name)
            dataBase = Room.databaseBuilder(context, AppDataBase::class.java, dbName).apply {
                allowMainThreadQueries()
                createFromFile(dbFile)
            }.build()
            return dataBase!!
        }

        fun execVacuum() {
            dataBase?.openHelper?.writableDatabase?.execSQL("VACUUM")
        }

        fun dbClose() {
            dataBase?.close()
            dataBase = null
        }

        private fun getMigrationFrom1To2(): Migration {
            return object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    val query = """CREATE TABLE IF NOT EXISTS "PlayList" (
	"_id"	INTEGER NOT NULL,
	"dict_name"	TEXT NOT NULL,
	"english"	TEXT NOT NULL,
	"translate"	TEXT NOT NULL,
	"count_repeat"	INTEGER NOT NULL DEFAULT 1,
	PRIMARY KEY("english")
);"""
                    if (db.isOpen) {
                        db.execSQL(query)
                    }
                }
            }
        }

        private fun getMigrationFrom2To3(): Migration {
            return object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    val query = """CREATE TABLE IF NOT EXISTS "History" (
	"video_id"	TEXT NOT NULL,
	"viewing_time"	INTEGER NOT NULL,
	"text"	TEXT NOT NULL,
	"thumbnail_url"	TEXT NOT NULL,
	"page_token"	TEXT NOT NULL,
	PRIMARY KEY("video_id")
);"""
                    if (db.isOpen) {
                        db.execSQL(query)
                    }
                }
            }
        }

    }

}