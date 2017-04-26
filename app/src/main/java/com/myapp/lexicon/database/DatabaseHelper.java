package com.myapp.lexicon.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.myapp.lexicon.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;


public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DB_PATH = "/data/com.myapp.lexicon/databases/";
    private static final String DB_NAME = "lexicon_DB"; // название бд
    private static final int version = 1; // версия базы данных

    // названия столбцов
    //public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ENGLISH = "English";
    public static final String COLUMN_TRANS = "Translate";
    public static final String COLUMN_IMAGE = "Image";
    public static final String COLUMN_Count_REPEAT = "CountRepeat";
    // имена служебных таблиц
    public static final String TABLE_API_KEY = "com_myapp_lexicon_api_keys";
    public static final String TABLE_METADATA = "android_metadata";
    public static final String TABLE_SEQUENCE = "sqlite_sequence";

    public SQLiteDatabase database;
    private Context context;
    private String actualPathDb;

    public DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

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
            boolean exists = fileDb.exists();
            if (!fileDb.exists())
            {
                Boolean res = directoryDb.mkdirs();
                this.database = SQLiteDatabase.openOrCreateDatabase(fileDb, null);
                //получаем локальную бд как поток
                myInput = context.getAssets().open(DB_NAME + ".db");
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
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            Toast.makeText(context, R.string.msg_error_creating_database, Toast.LENGTH_SHORT).show();
        }
    }

    private String getDbPath()
    {
        String stringPathDB = "";
        File pathToDB;
        try
        {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                pathToDB = context.getExternalCacheDir();
                if (pathToDB != null)
                {
                    stringPathDB = pathToDB.getPath();
                }
            }
            else
            {
                stringPathDB = Environment.getDataDirectory().toString() + DB_PATH;
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return stringPathDB;
    }

    public void open() throws SQLException
    {
        String path = actualPathDb;
        try
        {
            database = SQLiteDatabase.openDatabase(actualPathDb, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        catch (Exception e)
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
