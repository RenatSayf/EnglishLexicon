package com.myapp.lexicon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.StringOperations;

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
        String table_name = StringOperations.getInstance().spaceToUnderscore(tableName);
        int countEntries = 0;
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT count(RowId) FROM " + table_name, null);
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

    public boolean addTableToDbSync(final String tableName)
    {
        String table_name = StringOperations.getInstance().spaceToUnderscore(tableName);
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + table_name +
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

    public boolean deleteTableFromDbSync(final String tableName)
    {
        boolean result = true;
        String table_name = StringOperations.getInstance().spaceToUnderscore(tableName);
        String DELETE_TABLE = "Drop Table If Exists " + table_name;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.execSQL(DELETE_TABLE);
            }
        } catch (Exception e)
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
        String table_name = StringOperations.getInstance().spaceToUnderscore(tableName);
        long id = -1;
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.getEnglish());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.getTranslate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, 1);
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                id = databaseHelper.database.insert(table_name, null, values);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
        return id;
    }

    public long updateWordInTableSync(String tableName, long rowId, DataBaseEntry entry)
    {
        String table_name = StringOperations.getInstance().spaceToUnderscore(tableName);
        long id = -1;
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.getEnglish());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.getTranslate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, entry.getCountRepeat());

        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.beginTransaction();
                id = databaseHelper.database.update(table_name, values, "RowID = "+rowId, null);
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
            Toast.makeText(context,R.string.text_updated_successfully,Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context, R.string.text_write_error,Toast.LENGTH_SHORT).show();
        }

        return id;
    }

    public long deleteWordInTableSync(final String tableName, final long rowId)
    {
        String table_name = StringOperations.getInstance().spaceToUnderscore(tableName);
        long id = -1;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.beginTransaction();
                id = databaseHelper.database.delete(table_name, "RowId = " + rowId, null);

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
            Toast.makeText(context, R.string.text_updated_successfully,Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context, R.string.text_deleting_error,Toast.LENGTH_SHORT).show();
        }
        return id;
    }

    public void dataBaseVacuum(final String tableName)
    {
        String table_name = StringOperations.getInstance().spaceToUnderscore(tableName);
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.execSQL("VACUUM "+table_name);
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

    public String getApiKey()
    {
        String key = "";
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_API_KEY, null);
            }
            if (cursor != null && cursor.getCount() > 0)
            {
                if (cursor.moveToFirst())
                {
                    while ( !cursor.isAfterLast() )
                    {
                        key = cursor.getString(0);
                        cursor.moveToNext();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            databaseHelper.close();
        }
        return key;
    }

    public long addUpdateApiKey(String key)
    {
        long rowId = -1;
        ContentValues values = new ContentValues();
        values.put("key", key);
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.delete(DatabaseHelper.TABLE_API_KEY, null, null);
                rowId = databaseHelper.database.insert(DatabaseHelper.TABLE_API_KEY, null, values);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
        return rowId;
    }

}
