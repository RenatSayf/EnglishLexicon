package com.myapp.lexicon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;


public class DatabaseHelper extends SQLiteOpenHelper
{
    private static String DB_PATH = "/data/com.myapp.lexicon/databases/";  //"/data/data/com.myapp.lexicon/databases/";
    private static final String DB_NAME = "lexiconDB"; // название бд
    private static final int version = 1; // версия базы данных

    // названия столбцов
    //public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ENGLISH = "English";
    public static final String COLUMN_TRANS = "Translate";
    public static final String COLUMN_IMAGE = "Image";
    public static final String COLUMN_Count_REPEAT = "CountRepeat";

    public SQLiteDatabase database;
    private Context myContext;
    private String _actualPathDb;

    public DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, version);
        Log.i("Lexicon", "Заход в DatabaseHelper.Constructor");
        this.myContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i("Lexicon", "Заход в DatabaseHelper.onCreate");

//        for (String item : listTable)
//        {
//            db.execSQL("CREATE TABLE " + item + " (" +
//                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                    COLUMN_ENGLISH + " TEXT NOT NULL, " +
//                    COLUMN_TRANS + " TEXT NOT NULL" +
//                    COLUMN_IMAGE + " TEXT" +
//                    COLUMN_Count_REPEAT + " INTEGER);");
//        }

        // добавление начальных данных
//        db.execSQL("INSERT INTO "+ TABLE +" (" + COLUMN_ENGLISHQ
//                + ", " + COLUMN_TRANS  + ") VALUES ('Том Смит', 1981);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i("Lexicon", "Заход в DatabaseHelper.onUpgrade");

    }
    public void create_db()
    {
        Log.i("Lexicon", "Заход в DatabaseHelper.create_db()");
        String DB_PATH = getDbPath();
        InputStream myInput;
        OutputStream myOutput;
        try
        {
            File directoryDb = new File(DB_PATH);
            _actualPathDb = directoryDb.getAbsolutePath() + "/" + DB_NAME;
            File fileDb = new File(_actualPathDb);
            Log.i("Lexicon", "_actualPathDb - "+_actualPathDb);
            if (!fileDb.exists())
            {
                //this.getWritableDatabase();
                Boolean res = directoryDb.mkdirs();
                this.database = SQLiteDatabase.openOrCreateDatabase(fileDb, null);
                //получаем локальную бд как поток
                myInput = myContext.getAssets().open("lexiconDB.db");
                // Открываем пустую бд
                myOutput = new FileOutputStream(_actualPathDb);

                // побайтово копируем данные
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0)
                {
                    myOutput.write(buffer, 0, length);
                }

                myOutput.flush();
                myOutput.close();
                myInput.close();
            }
        }
        catch(IOException ex)
        {
            Log.i("Lexicon", "Исключение в DatabaseHelper.create_db() " + ex);
        }
        Log.i("Lexicon", "Выход из DatabaseHelper.create_db()");
    }

    private String getDbPath()
    {
        Log.i("Lexicon", "Вход в DatabaseHelper.getDbPath()");
        String stringPathDB = "";
        File pathToDB;
        try
        {
            if (!Environment.getExternalStorageState().equals("mounted"))
            {
                Log.i("Lexicon", "DatabaseHelper.getDbPath() current state of the primary \"external\" storage device is - mounted");
                pathToDB = myContext.getExternalCacheDir();
                stringPathDB = pathToDB.getPath();
                Log.i("Lexicon", "DatabaseHelper.getDbPath()  filePathStorage - "+pathToDB);
            }
            else
            {

                stringPathDB = Environment.getDataDirectory().toString() + DB_PATH;
                Log.i("Lexicon", "DatabaseHelper.getDbPath()  spathToDB - "+stringPathDB);
            }

        } catch (Exception e)
        {
            Log.i("Lexicon", "Исключение в DatabaseHelper.getDbPath() - " + e);
        }
        return stringPathDB;
    }

    public void open() throws SQLException
    {
        Log.i("Lexicon", "Заход в DatabaseHelper.open()");
        String path = Environment.getDataDirectory().toString() + DB_PATH + DB_NAME;
        try
        {
            database = SQLiteDatabase.openDatabase(_actualPathDb, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
            Log.i("Lexicon", "Выход из DatabaseHelper.open()");
        }
        catch (Exception e)
        {
            Log.i("Lexicon", "Исключение в DatabaseHelper.open() - " + e);
        }

    }
    @Override
    public synchronized void close()
    {
        Log.i("Lexicon", "Заход в DatabaseHelper.close()");
        try
        {
            if (database != null)
            {
                database.close();
            }
            super.close();
        }
        catch (Exception e)
        {
            Log.i("Lexicon", "Исключение в DatabaseHelper.close() - " + e );
        }

        Log.i("Lexicon", "Выход из DatabaseHelper.close()");
    }


}
