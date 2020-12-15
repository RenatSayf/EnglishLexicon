package com.myapp.lexicon.database

import android.content.ContentValues
import android.database.Cursor
import androidx.annotation.NonNull
import com.myapp.lexicon.helpers.StringOperations
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
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

    fun getTableListAsync() : Observable<MutableList<String>>
    {
        return Observable.create { emitter ->
            try
            {
                val tableList = getTableList()
                emitter.onNext(tableList)
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

    private fun getAllFromTable(tableName: String) : MutableList<DataBaseEntry>
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
                        dataBaseEntry = DataBaseEntry(cursor.getInt(0), tableName, cursor.getString(1), cursor.getString(2), cursor.getString(3))
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

    fun getAllFromTableAsync(tableName: String) : Single<MutableList<DataBaseEntry>>
    {
        return Single.create { emitter ->
            try
            {
                val entries = getAllFromTable(tableName)
                emitter.onSuccess(entries)
            }
            catch (e: Exception)
            {
                emitter.onError(e)
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

    private fun getEntriesFromDb(tableName: String, rowId: Int, order: String, limit: Int) : MutableList<DataBaseEntry>
    {
        val table = StringOperations.getInstance().spaceToUnderscore(tableName)
        val entriesFromDB = LinkedList<DataBaseEntry>()
        var dataBaseEntry: DataBaseEntry
        var cursor: Cursor? = null
        try
        {
            var compare = ">="
            if (order == "DESC") compare = "<="
            dbHelper.open()
            if (dbHelper.database.isOpen)
            {
                val cmd = "SELECT RowId, English, Translate, CountRepeat FROM $table WHERE RowId $compare $rowId AND CountRepeat <> 0 ORDER BY RowId $order LIMIT $limit"
                cursor = dbHelper.database.rawQuery(cmd, null)
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast)
                    {
                        dataBaseEntry = DataBaseEntry(cursor.getInt(0), tableName, cursor.getString(1), cursor.getString(2), cursor.getString(3))
                        entriesFromDB.add(dataBaseEntry)
                        cursor.moveToNext()
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return LinkedList<DataBaseEntry>()
        }
        finally
        {
            cursor?.close()
            dbHelper.close()
        }
        return entriesFromDB
    }

    private fun getRandomEntriesFromDb(tableName: String, rowId: Int) : MutableList<DataBaseEntry>
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
                val cmd = "SELECT RowId, English, Translate, CountRepeat FROM $table WHERE RowId <> $rowId ORDER BY random() LIMIT 1"
                cursor = dbHelper.database.rawQuery(cmd, null)
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast)
                    {
                        dataBaseEntry = DataBaseEntry(cursor.getInt(0), tableName, cursor.getString(1), cursor.getString(2), cursor.getString(3))
                        entriesFromDB.add(dataBaseEntry)
                        cursor.moveToNext()
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return LinkedList<DataBaseEntry>()
        }
        finally
        {
            cursor?.close()
            dbHelper.close()
        }
        return entriesFromDB
    }

    /**
     * @param order String сортировка по RowId, если не передавать этот
     * параметр, то order='ASC', для обратной сортировки передайте 'DESC'
     *
     * return Single<LinkedList<DataBaseEntry>> Возвращает 1 случайную запись из таблицы где ROWID записи
     * не равен параметру rowId
     */
    fun getRandomEntriesFromDbAsync(tableName: String, rowId: Int) : Single<MutableList<DataBaseEntry>>
    {
        return Single.create { emitter ->
            try
            {
                val entries = getRandomEntriesFromDb(tableName, rowId)
                emitter.onSuccess(LinkedList(entries))
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
        }
    }

    /**
     *
     * @param tableName - имя таблицы
     * amounts[0] - minimum RowId Where CountRepeat != 0
     * amounts[1] - maximum RowId Where CountRepeat != 0
     * amounts[2] - studied word amount
     * amounts[3] - total word amount
     */
    private fun getWordsCount(tableName: String) : MutableMap<String, Int>
    {
        var cursor: Cursor? = null
        val amounts = mutableMapOf<String, Int>()
        try
        {
            dbHelper.open()
            if (dbHelper.database.isOpen)
            {
                val cmd = "SELECT min(RowId) FROM $tableName WHERE (CountRepeat <> 0) UNION ALL SELECT max(RowId) FROM $tableName WHERE (CountRepeat <> 0) UNION ALL SELECT count(rowId) FROM $tableName WHERE CountRepeat == 0 UNION ALL SELECT count(rowId) FROM $tableName"
                cursor = dbHelper.database.rawQuery(cmd, null)
                if (cursor.moveToFirst())
                {
                    var i = 0
                    while (!cursor.isAfterLast)
                    {
                        try
                        {
                            var key = ""
                            when(i)
                            {
                                0 -> key = "minRowId"
                                1 -> key = "maxRowId"
                                2 -> key = "studiedWords"
                                3 -> key = "totalWords"
                            }
                            val int = cursor.getInt(0)
                            amounts.put(key, cursor.getInt(0))
                        }
                        catch (e: java.lang.Exception)
                        {
                            e.printStackTrace()
                        }
                        cursor.moveToNext()
                        i++
                    }
                }
            }
        }
        catch (e: java.lang.Exception)
        {
            e.printStackTrace()
            dbHelper.close()
        }
        finally
        {
            cursor?.close()
            dbHelper.close()
        }
        return amounts
    }

    fun getWordsCountAsync(tableName: String) : Single<MutableMap<String, Int>>
    {
        return Single.create { emitter ->
            try
            {
                val countList = getWordsCount(tableName)
                emitter.onSuccess(countList)
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
        }
    }

    @Suppress("RedundantSamConstructor")
    fun getEntriesAndCountersAsync(tableName: String, rowId: Int, order: String = "ASC", limit: Int = 2) : Observable<Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>>>
    {
        var countsList: MutableMap<String, Int> = mutableMapOf()
        return Observable.concat(ObservableSource { observer ->

            countsList = getWordsCount(tableName)
            try
            {
                observer.onNext(Pair(countsList, LinkedList<DataBaseEntry>()))
            }
            catch (e: Exception)
            {
                observer.onError(e)
            }
            finally
            {
                observer.onComplete()
            }
        }, ObservableSource { observer ->
            val entries = getEntriesFromDb(tableName, rowId, order, limit)
            try
            {
                observer.onNext(Pair(countsList, entries))
            }
            catch (e: Exception)
            {
                observer.onError(e)
            }
            finally
            {
                observer.onComplete()
            }
        })
    }

    private fun copyEntriesFromOtherTable(tableName: String) : Int
    {
        var res = -1
        try
        {
            dbHelper.open()
            dbHelper.database.beginTransaction()
            if (dbHelper.database.isOpen)
            {
                val cmd = "INSERT OR IGNORE INTO Words \n" +
                        "(english, translate)\n" +
                        "SELECT English, Translate \n" +
                        "FROM $tableName"
                val rawQuery = dbHelper.database.rawQuery(cmd, null)
                val v = ContentValues().apply {
                    put("dict_name", tableName)
                }
                res = dbHelper.database.update("Words", v, "dict_name = ", arrayOf("Общий"))
                dbHelper.database.setTransactionSuccessful()
                dbHelper.database.endTransaction()
            }
        }
        catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            dbHelper.close()
        }
        println("*************************** res = $res ****************************************")
        return res
    }

    fun copyEntriesFromOtherTableAsync(tableName: String) : Single<Int>
    {
        val list = mutableListOf("asdsa", "ddfgdfg")
        Observable.fromIterable(list).subscribe {

        }

        return Single.create { emitter ->
            try
            {
                val res = copyEntriesFromOtherTable(tableName)
                emitter.onSuccess(res)
            }
            catch (e: Exception)
            {
                emitter.onError(e)
            }
        }
    }



}



















