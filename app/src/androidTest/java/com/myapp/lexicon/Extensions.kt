package com.myapp.lexicon

import android.content.Context
import java.io.File
import java.io.FileOutputStream



fun Context.createTestDB(dbName: String = getString(R.string.test_db_name_1)): File {

    try {
        val existsTestBbName = this.databaseList().first {
            it == dbName
        }
        return File(existsTestBbName)
    } catch (e: NoSuchElementException) {
        val inputStream = this.assets.open("databases/$dbName")
        val bytes = inputStream.readBytes()
        val dbFolder = this.getDatabasePath(getString(R.string.data_base_name)).parent
        val file = File(dbFolder, dbName)
        if (!file.exists()) {
            file.createNewFile()
            FileOutputStream(file).apply {
                this.write(bytes)
                this.close()
            }
        }
        return file
    }
}