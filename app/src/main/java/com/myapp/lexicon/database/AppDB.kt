package com.myapp.lexicon.database

import android.database.Cursor
import androidx.annotation.NonNull
import com.myapp.lexicon.helpers.StringOperations
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.annotation.Nullable
import javax.inject.Inject

class AppDB @Inject constructor(private val dbHelper: DatabaseHelper)
{
    @NonNull
    private fun getTableList(): MutableList<String>
    {
        var nameNotDict: String
        var cursor: Cursor? = null
        val list = mutableListOf<String>()
        try
        {
            dbHelper.open()
            if (dbHelper.database.isOpen)
            {
                cursor = dbHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null)
            }
            if (cursor != null && cursor.count > 0)
            {
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast)
                    {
                        nameNotDict = cursor.getString(cursor.getColumnIndex("name"))
                        if (nameNotDict != DatabaseHelper.TABLE_METADATA && nameNotDict != DatabaseHelper.TABLE_SEQUENCE && nameNotDict != DatabaseHelper.TABLE_API_KEY)
                        {
                            var tableName = cursor.getString(cursor.getColumnIndex("name"))
                            tableName = StringOperations.getInstance().underscoreToSpace(tableName)
                            list.add(tableName)
                        }
                        cursor.moveToNext()
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            dbHelper.close()
        }
        finally
        {
            cursor?.close()
            dbHelper.close()
        }
        return list
    }

    fun getTableListAsync() : Observable<LinkedList<String>>
    {
        return Observable.create { emitter ->
            try
            {
                val tableList = getTableList()
                emitter.onNext(LinkedList(tableList))
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
            finally
            {
                emitter.onComplete()
            }
        }
    }

    fun getAllFromTable(tableName: String) : MutableList<DataBaseEntry>
    {
        val table = StringOperations.getInstance().spaceToUnderscore(tableName)
        val entriesFromDB = LinkedList<DataBaseEntry>()
        var dataBaseEntry: DataBaseEntry
        var cursor: Cursor? = null
        try
        {
            dbHelper.open()
            if (dbHelper.database.isOpen)
            {
                val cmd = "SELECT RowId, English, Translate, CountRepeat FROM $table WHERE CountRepeat <> 0"
                cursor = dbHelper.database.rawQuery(cmd, null)
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast)
                    {
                        dataBaseEntry = DataBaseEntry(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
                        entriesFromDB.add(dataBaseEntry)
                        cursor.moveToNext()
                    }
                }
            }
        }
        catch (e: java.lang.Exception)
        {
            e.printStackTrace()
            return LinkedList()
        }
        finally
        {
            cursor?.close()
            dbHelper.close()
        }
        return entriesFromDB
    }

    fun getAllFromTableAsync(tableName: String) : Observable<LinkedList<DataBaseEntry>>
    {
        return Observable.create { emitter ->
            try
            {
                val entries = getAllFromTable(tableName)
                emitter.onNext(LinkedList(entries))
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
            finally
            {
                emitter.onComplete()
            }
        }
    }

    private fun deleteTableFromDb(tableName: String): Boolean
    {
        var result = true
        val table = StringOperations.getInstance().spaceToUnderscore(tableName)
        val cmd = "Drop Table If Exists $table"
        try
        {
            dbHelper.open()
            if (dbHelper.database.isOpen)
            {
                dbHelper.database.execSQL(cmd)
            }
        }
        catch (e: java.lang.Exception)
        {
            result = false
            e.printStackTrace()
        }
        finally
        {
            dbHelper.close()
        }
        return result
    }

    fun deleteTableFromDbAsync(tableName: String) : Observable<Boolean>
    {
        return Observable.create { emitter ->
            try
            {
                val result = deleteTableFromDb(tableName)
                emitter.onNext(result)
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
            finally
            {
                emitter.onComplete()
            }
        }
    }

    fun dropTableFromDb(tableName: String) : Single<Boolean>
    {
        return Single.create { emitter ->
            try
            {
                val result = deleteTableFromDb(tableName)
                emitter.onSuccess(result)
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
        }
    }


}