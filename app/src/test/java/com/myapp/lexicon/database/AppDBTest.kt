package com.myapp.lexicon.database

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import androidx.test.platform.app.InstrumentationRegistry
import io.reactivex.disposables.Disposable
import org.junit.After

class AppDBTest
{
    private lateinit var appDB: AppDB
    private lateinit var disposable: Disposable

    @Before
    fun setUp()
    {
        val context = InstrumentationRegistry.getInstrumentation().context
        val dbHelper = DatabaseHelper(context)
        val roomDb = AppDataBase.buildDataBase(context).appDao()
        appDB = AppDB(dbHelper, roomDb)
    }

    @After
    fun finish()
    {
        disposable.dispose()
    }

    @Test
    fun migrateToWordsTable()
    {
        disposable = appDB.migrateToWordsTable()
    }
}