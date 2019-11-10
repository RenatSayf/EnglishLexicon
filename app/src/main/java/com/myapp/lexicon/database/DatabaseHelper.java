package com.myapp.lexicon.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import com.myapp.lexicon.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DB_PATH = "/data/com.myapp.lexicon/files/data/";
    private static String DB_NAME; // название бд
    private static final int version = 1; // версия базы данных

    // названия столбцов
    static final String COLUMN_ENGLISH = "English";
    static final String COLUMN_TRANS = "Translate";
    static final String COLUMN_IMAGE = "Image";
    public static final String COLUMN_Count_REPEAT = "CountRepeat";
    // имена служебных таблиц
    public static final String TABLE_API_KEY = "com_myapp_lexicon_api_keys";
    public static final String TABLE_METADATA = "android_metadata";
    public static final String TABLE_SEQUENCE = "sqlite_sequence";

    public SQLiteDatabase database;
    private Context context;
    private static String actualPathDb;

    public DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, version);
        this.context = context;
        try
        {
            DB_NAME = context.getString(R.string.data_base_name);
        } catch (Exception e)
        {
            Locale locale = Locale.getDefault();
            if ("uk".equals(locale.getLanguage()))
            {
                DB_NAME = "lexicon_uk_DB";
            } else
            {
                DB_NAME = "lexicon_DB";
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            db.disableWriteAheadLogging();
        }
    }

    public void create_db()
    {
        String DB_PATH = getDbPath();
        InputStream myInput;
        OutputStream myOutput;
        try
        {
            File directoryDb = new File(DB_PATH);
            actualPathDb = directoryDb.getAbsolutePath() + "/" + DB_NAME;
            File fileDb = new File(actualPathDb);
            if (!fileDb.exists())
            {
                try
                {
                    this.database = SQLiteDatabase.openOrCreateDatabase(fileDb, null);
                    this.database.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to create database", Toast.LENGTH_SHORT).show();
                    return;
                }
                //получаем локальную бд как поток
                myInput = context.getAssets().open(DB_NAME/* + ".db"*/);
                // Открываем пустую бд
                myOutput = new FileOutputStream(actualPathDb);

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
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private String getDbPath()
    {
        String stringPathDB;
        File pathToDB;
        try
        {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                pathToDB = context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath());
                //pathToDB = context.getExternalCacheDir();
                if (pathToDB != null)
                {
                    stringPathDB = pathToDB.getPath();
                } else
                {
                    stringPathDB = Environment.getDataDirectory().toString() + DB_PATH;
                }
            } else
            {
                stringPathDB = Environment.getDataDirectory().toString() + DB_PATH;
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            stringPathDB = Environment.getDataDirectory().toString() + DB_PATH;
        }
        return stringPathDB;
    }

    public void open()
    {
        try
        {
            database = SQLiteDatabase.openDatabase(actualPathDb, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close()
    {
        try
        {
            if (database != null)
            {
                database.close();
            }
            super.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getFilePath()
    {
        return actualPathDb;
    }


}
