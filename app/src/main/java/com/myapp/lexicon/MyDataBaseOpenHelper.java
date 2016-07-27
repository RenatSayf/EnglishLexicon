package com.myapp.lexicon;

import java.io.File;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public abstract class MyDataBaseOpenHelper
{

    private SQLiteDatabase db;
    private File dbFile;
    private String dbName;
    private boolean isExistDB;

    public MyDataBaseOpenHelper(Context context, String dbName)
    {
        super();
        this.isExistDB = false;
        this.dbName = dbName;
        this.prepareBD();
        if(!this.isExistDB)
        {
            onCreate(db);
        }
        db.close();
    }

    private void prepareBD()
    {
        try
        {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath () + "/" + dbName);
            directory.mkdirs();
            dbFile = new File(directory, dbName);
            if(dbFile.exists())this.isExistDB=true;
            this.db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
        catch (Exception e) {}
    }

    abstract public void onCreate(SQLiteDatabase db);

    abstract public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public void close()
    {
        db.close();
    }

    public SQLiteDatabase getReadableDatabase()
    {
        SQLiteDatabase resdDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        return resdDb;
    }

    public SQLiteDatabase getWritableDatabase()
    {
        SQLiteDatabase resdDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        return resdDb;


    }
}
