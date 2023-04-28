package com.myapp.lexicon

import android.content.Context
import java.io.File
import java.io.FileOutputStream

const val TEST_DB_NAME = "test_data_base.db"

fun Context.createTestDB() {
    try {
        this.databaseList().first {
            it == TEST_DB_NAME
        }
    } catch (e: NoSuchElementException) {
        val inputStream = this.assets.open("databases/$TEST_DB_NAME")
        val bytes = inputStream.readBytes()
        val dbPath = this.databaseList().first {
            it == "lexicon_DB.db"
        }
        val dbFolder = this.getDatabasePath(dbPath).parent
        val file = File(dbFolder, TEST_DB_NAME)
        if (!file.exists()) {
            file.createNewFile()
            FileOutputStream(file).apply {
                this.write(bytes)
                this.close()
            }
        }
    }
}