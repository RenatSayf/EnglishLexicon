package com.myapp.lexicon.database

import android.database.Cursor
import com.myapp.lexicon.helpers.StringOperations
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class AppDB @Inject constructor(private val dbHelper: DatabaseHelper)
{
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


}