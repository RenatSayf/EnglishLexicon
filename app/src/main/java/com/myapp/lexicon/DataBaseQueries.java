package com.myapp.lexicon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapp.lexicon.database.DatabaseHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ренат on 08.04.2016.
 */
public class DataBaseQueries
{
    private SQLiteDatabase _database;
    private static DatabaseHelper databaseHelper;
    private Handler handler;
    private Context context;
    public DataBaseQueries(SQLiteDatabase database)
    {

        if (database != null)
        {
            this._database = database;
        } else
        {
            Log.i("Lexicon", "DataBaseQueries Конструктор database = " + database);
        }
    }
    public DataBaseQueries(Context context) throws SQLException
    {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
        databaseHelper.create_db();
        databaseHelper.open();
    }

    public abstract static class GetWordsFromDBAsync extends AsyncTask<Object,Void,ArrayList<DataBaseEntry>>
    {
        public abstract void resultAsyncTask(ArrayList<DataBaseEntry> list);

        @Override
        protected ArrayList<DataBaseEntry> doInBackground(Object... params)
        {
            ArrayList<DataBaseEntry> entriesFromDB = getWordsFromDB((String) params[0], (int)params[1], (int)params[2]);
            return entriesFromDB;
        }

        @Override
        protected void onPostExecute(ArrayList<DataBaseEntry> list)
        {
            resultAsyncTask(list);
        }
    }


    public static ArrayList<DataBaseEntry> getWordsFromDB(String tableName, int startId, int endId)
    {
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        DataBaseEntry dataBaseEntry;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                Cursor cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId, null);
                int count = cursor.getCount();
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(3));
                        entriesFromDB.add(dataBaseEntry);
                        cursor.moveToNext();
                    }
                }
            } else
            {
                dataBaseEntry = new DataBaseEntry(null, null, null);
            }
        }
        catch (Exception e)
        {
            z_Log.v("Возникло исключение - "+e.getMessage());
            entriesFromDB.add(new DataBaseEntry(null,null, null));
        }
        finally
        {
            databaseHelper.database.close();
        }
        return entriesFromDB;
    }

    public ArrayList<DataBaseEntry> getEntriesFromDB(String tableName, int startId, int endId)
    {
        z_Log.v("tableName = " + tableName + "  startId = " + startId + "   endId = " + endId);
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                Cursor cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId, null);
                //Cursor cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowId = " + startId + " AND " + DatabaseHelper.COLUMN_ENGLISH + " NOTNULL", null);
                int count = cursor.getCount();
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        DataBaseEntry dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(3));
                        entriesFromDB.add(dataBaseEntry);
                        cursor.moveToNext();
                    }
                }
            } else
            {
                Log.i("Lexicon", "DataBaseQueries.getEntriesFromDB() _database.isOpen() = " + _database.isOpen());
            }
        }
        catch (Exception e)
        {
            Log.i("Lexicon", "Исключение в DataBaseQueries.getEntriesFromDB() = " + e);
            entriesFromDB.add(new DataBaseEntry(null,null, null));
        }
        finally
        {
            databaseHelper.database.close();
        }
        return entriesFromDB;
    }

    public ArrayList<DataBaseEntry> getEntriesFromDBAsync(final String tableName, final int startId, final int endId) throws SQLException, ExecutionException, InterruptedException
    {
        z_Log.v("tableName = " + tableName + "  startId = " + startId + "   endId = " + endId);
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected ArrayList<DataBaseEntry> doInBackground(Object[] params)
            {
                ArrayList<DataBaseEntry> entries = new ArrayList<>();
                try
                {
                    databaseHelper.open();
                    z_Log.v("databaseHelper.database.isOpen() = " + databaseHelper.database.isOpen());
                    if (databaseHelper.database.isOpen())
                    {
                        Cursor cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId + ";", null);
                        int count = cursor.getCount();
                        if (cursor.moveToFirst())
                        {
                            while (!cursor.isAfterLast())
                            {
                                DataBaseEntry dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(3));
                                entries.add(dataBaseEntry);
                                cursor.moveToNext();
                            }
                        }
                    } else
                    {
                        z_Log.v("  DataBaseQueries.getEntriesFromDBAsync() databaseHelper.database.isOpen() = " + databaseHelper.database.isOpen());
                    }
                }
                catch (Exception e)
                {
                    z_Log.v("  Исключение в DataBaseQueries.getEntriesFromDBAsync() = " + e);
                    entries.add(new DataBaseEntry(null,null,null));
                }
                finally
                {
                    databaseHelper.close();
                }
                return entries;
            }
        };
        asyncTask.execute();
        entriesFromDB = (ArrayList<DataBaseEntry>) asyncTask.get();

        return entriesFromDB;
    }

    public abstract static class GetWordsCountAsync extends AsyncTask<String, Void, Integer>
    {
        public abstract void resultAsyncTask(int res);
        @Override
        protected Integer doInBackground(String... params)
        {
            int count;
            try
            {
                databaseHelper.open();
                if (databaseHelper.database.isOpen())
                {
                    Cursor cursor=databaseHelper.database.query(params[0], null, null, null, null, null, null);
                    count = cursor.getCount();
                    z_Log.v("GetWordsCountAsync - " + count);
                } else
                {
                    z_Log.v("GetWordsCountAsync database.isOpen() = " + databaseHelper.database.isOpen());
                    count = 0;
                }
            }
            catch (Exception e)
            {
                z_Log.v("ИСКЛЮЧЕНИЕ - " + e.getMessage());
                count = 0;
            }finally
            {
                databaseHelper.close();
            }
            return count;
        }

        @Override
        protected void onPostExecute(Integer integer)
        {
            resultAsyncTask(integer);
        }
    }
    public int getWordsCount(String dictName)
    {
        int count;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                Cursor cursor=databaseHelper.database.query(dictName, null, null, null, null, null, null);
                count = cursor.getCount();
                z_Log.v("getWordsCount() - " + count);
            } else
            {
                z_Log.v("getWordsCount _database.isOpen() = " + databaseHelper.database.isOpen());
                count = 0;
            }
        }
        catch (Exception e)
        {
            z_Log.v("ИСКЛЮЧЕНИЕ .getWordsCount() - " + e);
            count = 0;
        }finally
        {
            databaseHelper.close();
        }
        return count;
    }
    public int getEntriesCountAsync(final String tableName) throws ExecutionException, InterruptedException, SQLException
    {
        int countEntries = 0;
        AsyncTask asyncTask = new AsyncTask()
        {
            int count = 0;
            @Override
            protected Object doInBackground(Object[] params)
            {
                try
                {
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        Cursor cursor=databaseHelper.database.query(tableName, null, null, null, null, null, null);
                        count = cursor.getCount();
                        z_Log.v("getEntriesCountAsync() - " + count);
                    } else
                    {
                        z_Log.v("getEntriesCountAsync databaseHelper.database.isOpen() = " + databaseHelper.database.isOpen());
                        count = 0;
                    }
                }
                catch (Exception e)
                {
                    z_Log.v("ИСКЛЮЧЕНИЕ .getEntriesCountAsync() - " + e);
                    count = 0;
                }finally
                {
                    databaseHelper.close();
                }
                return count;
            }
        };
        asyncTask.execute();
        countEntries = (int) asyncTask.get();
        return countEntries;
    }
    public boolean addTableToDbAsync(final String tableName) throws ExecutionException, InterruptedException
    {
        boolean res;
        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                String name = checkTableName(tableName);
                String CREATE_TABLE="CREATE TABLE IF NOT EXISTS "+name+
                        "("+
                        //DatabaseHelper.COLUMN_ID+" INTEGER PRIMARY KEY ASC, "+
                        DatabaseHelper.COLUMN_ENGLISH+" VARCHAR NOT NULL, "+
                        DatabaseHelper.COLUMN_TRANS+" VARCHAR NOT NULL, "+
                        DatabaseHelper.COLUMN_IMAGE+" VARCHAR NULL, "+
                        DatabaseHelper.COLUMN_Count_REPEAT+" VARCHAR NULL"+
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
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    result = false;
                }
                finally
                {
                    databaseHelper.close();
                }
                return result;
            }
        };
        asyncTask.execute();
        res = (boolean) asyncTask.get();
        return res;
    }
    private String checkTableName(String tableName)
    {
        String name = tableName.trim();
        return name.replace(' ','_');
    }
    public boolean deleteTableFromDbAsync(final String tableName) throws ExecutionException, InterruptedException
    {
        boolean result = true;

        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
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
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    result = false;
                    e.printStackTrace();
                }
                finally
                {
                    databaseHelper.close();
                }
                return result;
            }
        };
        asyncTask.execute();
        result = (boolean) asyncTask.get();
        return result;
    }
    public String[] setListTableToSpinner() throws ExecutionException, InterruptedException
    {
        String[] listTable;
        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                ArrayList<String> list=new ArrayList<>();
                String nameNotDict;
                String[] listTable = new String[0];
                try
                {
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        Cursor cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
                        if (cursor.moveToFirst())
                        {
                            while ( !cursor.isAfterLast() )
                            {
                                nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                                if (!nameNotDict.equals("android_metadata") && !nameNotDict.equals("sqlite_sequence"))
                                {
                                    list.add( cursor.getString( cursor.getColumnIndex("name")) );
                                }
                                cursor.moveToNext();
                            }
                        }
                        listTable=new String[list.size()];
                        if (list.size() > 0)
                        {
                            int i=0;
                            for (String item : list)
                            {
                                listTable[i]=item;
                                i++;
                            }
                        }
                    }
                } catch (SQLException e)
                {
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    e.printStackTrace();
                }
                finally
                {
                    databaseHelper.close();
                }
                return listTable;
            }
        };
        asyncTask.execute();
        listTable = (String[]) asyncTask.get();
        return listTable;
    }

    public abstract static class GetLictTableAsync extends AsyncTask<Void, Void, ArrayList<String>>
    {
        public abstract void resultAsyncTask(ArrayList<String> list);

        @Override
        protected ArrayList<String> doInBackground(Void... params)
        {
            final ArrayList<String> arrayList = new ArrayList<>();
            String nameNotDict;
            try
            {
                databaseHelper.open();
                if (databaseHelper.database.isOpen())
                {
                    Cursor cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
                    if (cursor.moveToFirst())
                    {
                        while ( !cursor.isAfterLast() )
                        {
                            nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                            if (!nameNotDict.equals("android_metadata") && !nameNotDict.equals("sqlite_sequence"))
                            {
                                arrayList.add( cursor.getString( cursor.getColumnIndex("name")) );
                            }
                            cursor.moveToNext();
                        }
                    }
                }
            } catch (SQLException e)
            {
                z_Log.v("ИСКЛЮЧЕНИЕ - "+e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                databaseHelper.close();
            }
            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> arrayList)
        {
            resultAsyncTask(arrayList);
        }
    }

    public void setListTableToSpinner(final Spinner spinner, final int selection)
    {
        final ArrayList<String> list = new ArrayList<>();
        final AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected ArrayList<String> doInBackground(Object[] params)
            {
                String nameNotDict;
                try
                {
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        Cursor cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
                        if (cursor.moveToFirst())
                        {
                            while ( !cursor.isAfterLast() )
                            {
                                nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                                if (!nameNotDict.equals("android_metadata") && !nameNotDict.equals("sqlite_sequence"))
                                {
                                    list.add( cursor.getString( cursor.getColumnIndex("name")) );
                                }
                                cursor.moveToNext();
                            }
                        }
                    }
                } catch (SQLException e)
                {
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    e.printStackTrace();
                }
                finally
                {
                    databaseHelper.close();
                }
                return list;
            }

            @Override
            protected void onPostExecute(Object array)
            {
                ArrayList<String> arrayList = (ArrayList<String>) array;
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(context, R.layout.my_content_spinner_layout, arrayList);
                spinner.setAdapter(adapterSpinner);
                if (selection >= arrayList.size())
                {
                    spinner.setSelection(0);
                }
                if (selection >= 0 && selection < arrayList.size())
                {
                    spinner.setSelection(selection);
                }
            }
        };
        asyncTask.execute();
    }
    public long insertWordInTable(String tableName, DataBaseEntry entry)
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
            z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
        return id;
    }
    public long updateWordInTable(final String tableName, final String old_en, final String old_ru, DataBaseEntry entry) throws Exception
    {
        final long[] id = {-1};
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.get_english());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.get_translate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, 1);

        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                try
                {
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        databaseHelper.database.beginTransaction();
                        id[0] = databaseHelper.database.update(tableName, values, DatabaseHelper.COLUMN_ENGLISH +
                                " = ? AND " + DatabaseHelper.COLUMN_TRANS + " = ?", new String[] {old_en, old_ru});
                        databaseHelper.database.setTransactionSuccessful();
                        databaseHelper.database.endTransaction();
                    }
                } catch (Exception e)
                {
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    e.printStackTrace();
                }
                finally
                {
                    databaseHelper.close();
                }

                return id[0];
            }

            @Override
            protected void onPostExecute(Object id)
            {
                long _id = (long) id;
                if (_id != -1)
                {
                    Toast.makeText(context,"Словарь успешно обновлен",Toast.LENGTH_SHORT).show();
                    z_Log.v("_id = "+_id);
                }
                else
                {
                    Toast.makeText(context,"Ошибка записи",Toast.LENGTH_SHORT).show();
                    z_Log.v("_id = "+_id);
                }
            }
        };
        asyncTask.execute();
        id[0] = (long) asyncTask.get();
        return id[0];
    }

    public long updateWordInTable(final String tableName, final long rowId, DataBaseEntry entry) throws Exception
    {
        final long[] id = {-1};
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.get_english());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.get_translate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, entry.get_count_repeat());

        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                try
                {
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        databaseHelper.database.beginTransaction();
                        //id[0] = databaseHelper.database.update(tableName, values, DatabaseHelper.COLUMN_ID+" = "+rowId, null);
                        id[0] = databaseHelper.database.update(tableName, values, "RowID = "+rowId, null);
                        databaseHelper.database.setTransactionSuccessful();
                        databaseHelper.database.endTransaction();
                    }
                } catch (Exception e)
                {
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    e.printStackTrace();
                }
                finally
                {
                    databaseHelper.close();
                }

                return id[0];
            }

            @Override
            protected void onPostExecute(Object id)
            {
                long _id = (long) id;
                if (_id != -1)
                {
                    Toast.makeText(context,"Словарь успешно обновлен",Toast.LENGTH_SHORT).show();
                    z_Log.v("_id = "+_id);
                }
                else
                {
                    Toast.makeText(context,"Ошибка записи",Toast.LENGTH_SHORT).show();
                    z_Log.v("_id = "+_id);
                }
            }
        };
        asyncTask.execute();
        id[0] = (long) asyncTask.get();
        return id[0];
    }

    public long deleteWordInTable(final String tableName, final long rowId) throws ExecutionException, InterruptedException
    {
        final long[] id = {-1};

        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                try
                {
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        databaseHelper.database.beginTransaction();
                        id[0] = databaseHelper.database.delete(tableName, "RowId = " + rowId, null);

                        databaseHelper.database.setTransactionSuccessful();
                        databaseHelper.database.endTransaction();
                    }
                } catch (Exception e)
                {
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    e.printStackTrace();
                }
                finally
                {
                    databaseHelper.close();
                }

                return id[0];
            }

            @Override
            protected void onPostExecute(Object id)
            {
                long _id = (long) id;
                if (_id != -1)
                {
                    Toast.makeText(context,"Словарь успешно обновлен",Toast.LENGTH_SHORT).show();
                    z_Log.v("_id = "+_id);
                }
                else
                {
                    Toast.makeText(context,"Ошибка записи",Toast.LENGTH_SHORT).show();
                    z_Log.v("_id = "+_id);
                }
            }
        };
        asyncTask.execute();
        id[0] = (long) asyncTask.get();
        return id[0];
    }
    public void dataBaseVacuum(final String tableName) throws ExecutionException, InterruptedException
    {
        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
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
                    z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
                    e.printStackTrace();
                }
                finally
                {
                    databaseHelper.close();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object id)
            {
                Toast.makeText(context,"Vacuum Ok",Toast.LENGTH_SHORT).show();

            }
        };
        asyncTask.execute();
        asyncTask.get();
    }

    public abstract static class GetRowIdOfWordAsync extends AsyncTask<String, Void, Integer>
    {
        public abstract void resultAsyncTask(Integer id);

        @Override
        protected Integer doInBackground(String... params)
        {
            int id = -1;
            try
            {
                id = (int) getIdOfWord(params[0], params[1], params[2]);
            } catch (ExecutionException e)
            {
                e.printStackTrace();
            } catch (InterruptedException e)
            {
                z_Log.v("Возникло исключение - "+e.getMessage());
                e.printStackTrace();
            }

            return id;
        }

        @Override
        protected void onPostExecute(Integer id)
        {
            resultAsyncTask(id);
        }
    }

    public static long getIdOfWord(final String tableName, final String english, final String translate) throws ExecutionException, InterruptedException
    {
        final long[] id = {-1};

        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.beginTransaction();
                Cursor cursor = databaseHelper.database.rawQuery(
                        "SELECT RowID FROM '"+tableName+
                                "' Where "+DatabaseHelper.COLUMN_ENGLISH+
                                " = '"+english+
                                "' AND "+DatabaseHelper.COLUMN_TRANS+
                                " = '"+translate+"'", null);
                databaseHelper.database.setTransactionSuccessful();
                databaseHelper.database.endTransaction();
                if (cursor != null && cursor.moveToFirst())
                {
                    id[0] = cursor.getLong(0);
                }
            }
        } catch (SQLException e)
        {
            z_Log.v("ИСКЛЮЧЕНИЕ - "+e);
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }
        return id[0];
    }

}
