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
import com.myapp.lexicon.helpers.getCRC32CheckSum
import com.myapp.lexicon.helpers.printLogIfDebug
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.saveInitDbCheckSum


private const val DB_VERSION = 2


@Database(entities = [Word::class, WordToPlay::class], version = DB_VERSION, exportSchema = true)
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
                addMigrations(getMigrationFrom1To2())
            }.build().apply {
                val checkSum = context.assets.open("databases/$dbName").readBytes().getCRC32CheckSum()
                context.saveInitDbCheckSum(checkSum)
                printLogIfDebug("************** Database init check sum = $checkSum **********************")
            }
            return dataBase as AppDataBase
        }

        fun execVacuum() {
            dataBase?.openHelper?.writableDatabase?.execSQL("VACUUM")
        }

        fun dbClose() {
            dataBase?.close()
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
                    Thread(Runnable {
                        db.execSQL(query)
                    }).start()
                }
            }
        }

    }

}