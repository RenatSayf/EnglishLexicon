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
import java.io.File


private const val DB_VERSION = 2


@Database(entities = [Word::class, WordToPlay::class], version = DB_VERSION, exportSchema = true)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun appDao(): AppDao

    companion object
    {
        private var dataBase: AppDataBase? = null

        fun getDbInstance(context: Context): AppDataBase {
            return if (dataBase == null) {
                dataBase = buildDataBase(context)
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

                val dbFile = context.getDatabasePath(dbName)
                if (dbFile.exists()) {
                    createFromFile(dbFile)
                }
                else {
                    createFromAsset("databases/$dbName")
                    addMigrations(getMigrationFrom1To2())
                }
            }.build().apply {
                val checkSum = context.assets.open("databases/$dbName").readBytes().getCRC32CheckSum()
                context.saveInitDbCheckSum(checkSum)
                printLogIfDebug("************** Database init check sum = $checkSum **********************")
            }
            return dataBase as AppDataBase
        }

        fun buildDataBaseFromFile(context: Context, dbFile: File): AppDataBase {
            val dbName = context.getString(R.string.data_base_name)
            dataBase = Room.databaseBuilder(context, AppDataBase::class.java, dbName).apply {
                createFromFile(dbFile)
            }.build().apply {
                val checkSum = dbFile.readBytes().getCRC32CheckSum()
                printLogIfDebug("************** Database init check sum = $checkSum **********************")
            }
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
                    Thread(Runnable {
                        db.execSQL(query)
                    }).start()
                }
            }
        }

    }

}