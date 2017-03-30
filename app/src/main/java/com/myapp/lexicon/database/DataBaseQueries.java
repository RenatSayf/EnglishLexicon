package com.myapp.lexicon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.myapp.lexicon.helpers.MyLog;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * Synchronous queries to database
 */
public class DataBaseQueries
{
    private DatabaseHelper databaseHelper;
    private Context context;

    public DataBaseQueries(Context context)
    {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
        databaseHelper.create_db();
    }

    public int getCountEntriesSync(final String tableName)
    {
        int countEntries = 0;
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT count(RowId) FROM " + tableName, null);
                cursor.moveToFirst();
                countEntries = cursor.getInt(0);
            } else
            {
                countEntries = 0;
            }
        }
        catch (Exception e)
        {
            countEntries = 0;
        }finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            databaseHelper.close();
        }
        return countEntries;
    }

    public boolean addTableToDbSync(final String tableName) throws ExecutionException, InterruptedException
    {
        String name = checkTableName(tableName);
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + name +
                "(" +
                //DatabaseHelper.COLUMN_ID+" INTEGER PRIMARY KEY ASC, "+
                DatabaseHelper.COLUMN_ENGLISH + " VARCHAR NOT NULL, " +
                DatabaseHelper.COLUMN_TRANS + " VARCHAR NOT NULL, " +
                DatabaseHelper.COLUMN_IMAGE + " VARCHAR NULL, " +
                DatabaseHelper.COLUMN_Count_REPEAT + " VARCHAR NULL" +
                ")";
        boolean result = true;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.execSQL(CREATE_TABLE);
            }
        }
        catch (Exception e)
        {
            result = false;
        }
        finally
        {
            databaseHelper.close();
        }
        return result;
    }

    private String checkTableName(String tableName)
    {
        String name = tableName.trim();
        return name.replace(' ','_');
    }

    public boolean deleteTableFromDbSync(final String tableName)
    {
        boolean result = true;
        String name = checkTableName(tableName);
        String DELETE_TABLE = "Drop Table If Exists " + name;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.execSQL(DELETE_TABLE);
            }
        } catch (SQLException e)
        {
            result = false;
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
        return result;
    }

    public long insertWordInTableSync(String tableName, DataBaseEntry entry)
    {
        long id = -1;
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.get_english());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.get_translate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, 1);
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                id = databaseHelper.database.insert(tableName, null, values);
            }
        } catch (SQLException e)
        {
            MyLog.v("ИСКЛЮЧЕНИЕ - "+e);
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
        return id;
    }

    public long updateWordInTableSync(String tableName, long rowId, DataBaseEntry entry) throws Exception
    {
        long id = -1;
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.get_english());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.get_translate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, entry.get_count_repeat());

        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.beginTransaction();
                id = databaseHelper.database.update(tableName, values, "RowID = "+rowId, null);
                databaseHelper.database.setTransactionSuccessful();
                databaseHelper.database.endTransaction();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }

        if (id != -1)
        {
            Toast.makeText(context,"Словарь успешно обновлен",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context,"Ошибка записи",Toast.LENGTH_SHORT).show();
        }

        return id;
    }

    public long deleteWordInTableSync(final String tableName, final long rowId)
    {
        long id = -1;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.beginTransaction();
                id = databaseHelper.database.delete(tableName, "RowId = " + rowId, null);

                databaseHelper.database.setTransactionSuccessful();
                databaseHelper.database.endTransaction();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
        if (id != -1)
        {
            Toast.makeText(context,"Словарь успешно обновлен",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context,"Ошибка записи",Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public void dataBaseVacuum(final String tableName)
    {
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.execSQL("VACUUM "+tableName);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
    }

}
