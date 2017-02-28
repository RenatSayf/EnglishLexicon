package com.myapp.lexicon;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetEntriesLoader;

import java.sql.SQLException;
import java.util.ArrayList;

public class d_WordEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private Spinner spinnerListDict;
    private int spinner_select_pos = -1;
    private SearchView searchView;
    private ListView listView;
    private ImageButton buttonWrite;
    private ImageButton buttonDelete;
    private ImageButton buttonCancel;
    private EditText editTextEn, editTextRu;
    private Spinner spinnerCountRepeat, spinnerListDict2;
    private CheckBox checkCopy, checkMove;
    private LinearLayout layoutSpinner;
    private static d_ListViewAdapter listViewAdapter;
    private ArrayList<DataBaseEntry> dataBaseEntries;
    private Handler handler;
    private ProgressBar progressBar;
    private DatabaseHelper _databaseHelper;
    private DataBaseQueries dataBaseQueries;
    private ViewSwitcher switcher;
    private Animation slide_in_left, slide_out_right;
    private z_LockOrientation lockOrientation;

    private static boolean searchIsVisible = false;

    private String KEY_SWITCHER_DISPLAYED_CHILD = "sw-d-ch";
    private String KEY_SPINNER_SELECT_INDEX = "sp-slt-idx";
    private String KEY_SPINNER_COUNT_REPEAT_SELECT_INDEX = "sp-cnt-rep-slt-idx";
    private String KEY_SPINNER_ITEMS = "sp-items";
    private String KEY_SEARCH_QUERY = "srch-query";
    private String KEY_EDITTEXT_EN = "edit-txt-en";
    private String KEY_EDITTEXT_RU = "edit-txt-ru";
    private String KEY_CHECK_COPY = "check-copy";
    private String KEY_CHECK_MOVE = "check-move";


    private void initViews()
    {
        spinnerListDict =(Spinner)findViewById(R.id.spinner);

        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setVisibility(View.GONE);
        searchView_onListeners();

        listView=(ListView) findViewById(R.id.listView);

        progressBar= (ProgressBar) findViewById(R.id.progressBar);

        switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);
        switcher.setInAnimation(slide_in_left);
        switcher.setOutAnimation(slide_out_right);
        
        buttonWrite = (ImageButton) findViewById(R.id.btn_write);
        buttonWrite.setEnabled(true);

        buttonDelete = (ImageButton) findViewById(R.id.btn_delete);

        buttonCancel = (ImageButton) findViewById(R.id.btn_cancel);

        editTextEn = (EditText) findViewById(R.id.edit_text_en);
        editTextRu = (EditText) findViewById(R.id.edit_text_ru);
        spinnerCountRepeat = (Spinner) findViewById(R.id.spinn_cout_repeat);
        spinnerListDict2 = (Spinner) findViewById(R.id.spinn_dict_to_move);

        checkCopy = (CheckBox) findViewById(R.id.check_copy);

        checkMove = (CheckBox) findViewById(R.id.check_move);

        layoutSpinner = (LinearLayout) findViewById(R.id.lin_layout_spin);
        layoutSpinner.setVisibility(View.GONE);

        spinner_OnItemSelected();
        listView_OnItemClick();
        buttonWrite_OnClick();
        buttonDelete_OnClick();
        buttonCancel_OnClick();
        checkCopy_OnClick();
        checkMove_OnClick();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(KEY_SWITCHER_DISPLAYED_CHILD, switcher.getDisplayedChild());
        outState.putInt(KEY_SPINNER_SELECT_INDEX, spinnerListDict.getSelectedItemPosition());
        outState.putInt(KEY_SPINNER_COUNT_REPEAT_SELECT_INDEX, spinnerCountRepeat.getSelectedItemPosition());
        outState.putString(KEY_SEARCH_QUERY, searchView.getQuery().toString());
        ArrayList<String> spinnerItems = new ArrayList<>();
        for (int i = 0; i < spinnerListDict.getCount(); i++)
        {
            spinnerItems.add(spinnerListDict.getItemAtPosition(i).toString());
        }
        outState.putStringArrayList(KEY_SPINNER_ITEMS, spinnerItems);
        outState.putString(KEY_EDITTEXT_EN, editTextEn.getText().toString());
        outState.putString(KEY_EDITTEXT_RU, editTextRu.getText().toString());
        outState.putBoolean(KEY_CHECK_COPY, checkCopy.isChecked());
        outState.putBoolean(KEY_CHECK_MOVE, checkMove.isChecked());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_layout_word_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lockOrientation = new z_LockOrientation(this);

        try
        {
            dataBaseQueries = new DataBaseQueries(this);
        } catch (SQLException e)
        {
            Toast.makeText(this,getString(R.string.msg_data_base_error)+e.getMessage(),Toast.LENGTH_SHORT).show();
            this.finish();
        }
        if (_databaseHelper == null)
        {
            _databaseHelper = new DatabaseHelper(this);
            _databaseHelper.create_db();
        }

        initViews();

        dataBaseQueries.setListTableToSpinner(spinnerListDict2,0);

        if (savedInstanceState == null)
        {
            spinnerCountRepeat.setSelection(1);
        }

        // TODO: AsyncTaskLoader - 3. инициализация
        getLoaderManager().initLoader(LOADER_GET_ENTRIES, savedInstanceState, this);

        if (savedInstanceState != null)
        {
            if (searchIsVisible)
            {
                searchView.setVisibility(View.VISIBLE);
                searchView.setQuery(savedInstanceState.getString(KEY_SEARCH_QUERY), false);
            }

            switcher.setDisplayedChild(savedInstanceState.getInt(KEY_SWITCHER_DISPLAYED_CHILD));
            ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(this, R.layout.my_content_spinner_layout, savedInstanceState.getStringArrayList(KEY_SPINNER_ITEMS));
            spinnerListDict.setAdapter(adapterSpinner);
            spinnerListDict.setSelection(savedInstanceState.getInt(KEY_SPINNER_SELECT_INDEX));
            spinnerCountRepeat.setSelection(savedInstanceState.getInt(KEY_SPINNER_COUNT_REPEAT_SELECT_INDEX));
            spinner_select_pos = spinnerListDict.getSelectedItemPosition();
            listViewSetSource(false);

            editTextEn.setText(savedInstanceState.getString(KEY_EDITTEXT_EN));
            editTextRu.setText(savedInstanceState.getString(KEY_EDITTEXT_RU));
            checkCopy.setChecked(savedInstanceState.getBoolean(KEY_CHECK_COPY));
            checkMove.setChecked(savedInstanceState.getBoolean(KEY_CHECK_MOVE));
            if (checkMove.isChecked())
            {
                layoutSpinner.setVisibility(View.VISIBLE);
            }
            else
            {
                layoutSpinner.setVisibility(View.GONE);
            }
        }
        else
        {
            dataBaseQueries.setListTableToSpinner(spinnerListDict,0);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            String tableName = bundle.getString(a_MainActivity.KEY_DICT_NAME);
            int rowId = bundle.getInt(a_MainActivity.KEY_ROW_ID);

            // TODO: AsyncTaskLoader - 4. Передача параметров в AsyncTaskLoader
            Bundle loaderBundle = new Bundle();
            loaderBundle.putString(GetEntriesLoader.KEY_TABLE_NAME, tableName);
            loaderBundle.putInt(GetEntriesLoader.KEY_START_ID, rowId);
            loaderBundle.putInt(GetEntriesLoader.KEY_END_ID, rowId);

            // TODO: AsyncTaskLoader - 5. Запуск загрузки данных
            Loader<Cursor> cursorLoader = getLoaderManager().restartLoader(LOADER_GET_ENTRIES, loaderBundle, this);
            cursorLoader.forceLoad();

            switcher.showNext();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void spinner_OnItemSelected()
    {
        spinnerListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(spinner_select_pos == position) return;
                progressBar.setVisibility(View.VISIBLE);
                listViewSetSource(true);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
    }

    private void listViewSetSource(final boolean update)
    {
        if (update)
        {
            // TODO:  Handler() асинхронная загрузка данных в ListView
            new Thread(new Runnable()
            {
                public void run()
                {
                    dataBaseEntries = getEntriesFromDB(spinnerListDict.getSelectedItem().toString());
                    // TODO: ListView создание экземпляра адаптера
                    listViewAdapter = new d_ListViewAdapter(dataBaseEntries, d_WordEditor.this, R.id.search_view);
                    try
                    {
                        handler.sendEmptyMessage(0);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();

            handler=new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    listView.setAdapter(listViewAdapter); // TODO: ListView setAdapter
                    progressBar.setVisibility(View.GONE);
                }
            };
        }
        else
        {
            listView.setAdapter(listViewAdapter);
            progressBar.setVisibility(View.GONE);
        }
    }

    private static long rowID;
    private static String testTextEn;
    private static String testTextRu;
    private static String testCurrentDict;
    private static int testCountRepeat;
    private void listView_OnItemClick()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                TextView textViewEn = (TextView) view.findViewById(R.id.english);
                testTextEn = textViewEn.getText().toString();
                editTextEn.setText(textViewEn.getText().toString());

                TextView textViewRu = (TextView) view.findViewById(R.id.translate);
                testTextRu = textViewRu.getText().toString();
                editTextRu.setText(textViewRu.getText().toString());

                TextView textViewCounRepeat = (TextView) view.findViewById(R.id.count_repeat);
                testCountRepeat = Integer.parseInt(textViewCounRepeat.getText().toString());
                spinnerCountRepeat.setSelection(testCountRepeat);

                String tableName = spinnerListDict.getSelectedItem().toString();
                testCurrentDict = spinnerListDict.getSelectedItem().toString();
                try
                {
                    rowID = dataBaseQueries.getIdOfWord(tableName, testTextEn, testTextRu);

                } catch (Exception e)
                {
                    Toast.makeText(d_WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                checkMove.setChecked(false);
                layoutSpinner.setVisibility(View.GONE);
                switcher.showNext();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                //z_Log.v("Выбран долгим нажатием - "+position);

                return false;
            }
        });
    }

    private void buttonCancel_OnClick()
    {
        buttonCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switcher.showPrevious();
            }
        });
    }

    private void buttonDelete_OnClick()
    {
        buttonDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                lockOrientation.lock();
                final String tableName = spinnerListDict.getSelectedItem().toString();

                new AlertDialog.Builder(d_WordEditor.this) // TODO: 26.01.2017 AlertDialog с макетом по умолчанию
                        .setTitle(R.string.dialog_title_confirm_action)
                        .setIcon(R.drawable.icon_warning)
                        .setMessage(getString(R.string.dialog_msg_delete_word) + tableName + "?")
                        .setPositiveButton(R.string.button_text_yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                try
                                {
                                    dataBaseQueries.deleteWordInTable(tableName, rowID);
                                    dataBaseQueries.dataBaseVacuum(tableName);
                                } catch (Exception e)
                                {
                                    Toast.makeText(d_WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    d_WordEditor.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                }
                                listViewSetSource(true);
                                switcher.showPrevious();
                                d_WordEditor.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            }
                        })
                        .setNegativeButton(R.string.button_text_no, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                d_WordEditor.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                return;
                            }
                        })
                        .create().show();
            }
        });
    }

    private void buttonWrite_OnClick()
    {
        buttonWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (editTextEn.getText().toString().equals("") || editTextRu.getText().toString().equals(""))
                {
                    return;
                }
                if (editTextEn.getText().toString().equals(testTextEn) && editTextRu.getText().toString().equals(testTextRu) && Integer.parseInt(spinnerCountRepeat.getSelectedItem().toString()) == testCountRepeat && spinnerListDict2.getSelectedItem().toString().equals(testCurrentDict))
                {
                    return;
                }

                z_StringOperations stringOperations = z_StringOperations.getInstance();
                if (stringOperations.getLangOfText(editTextEn.getText().toString())[1].equals("en") &&
                        stringOperations.getLangOfText(editTextRu.getText().toString())[1].equals("ru"))
                {
                    String tableName = spinnerListDict.getSelectedItem().toString();
                    String new_table_name = spinnerListDict2.getSelectedItem().toString();
                    DataBaseEntry baseEntry = new DataBaseEntry(editTextEn.getText().toString(), editTextRu.getText().toString(), null, spinnerCountRepeat.getSelectedItem().toString());
                    if (!checkMove.isChecked())
                    {
                        try
                        {
                            dataBaseQueries.updateWordInTable(tableName, rowID, baseEntry);
                        } catch (Exception e)
                        {
                            Toast.makeText(d_WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (checkMove.isChecked() && !checkCopy.isChecked())
                    {
                        try
                        {
                            dataBaseQueries.deleteWordInTable(tableName, rowID);
                            dataBaseQueries.dataBaseVacuum(tableName);
                        } catch (Exception e)
                        {
                            Toast.makeText(d_WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dataBaseQueries.insertWordInTable(new_table_name, baseEntry);
                    }
                    else if (checkMove.isChecked() && checkCopy.isChecked())
                    {
                        try
                        {
                            dataBaseQueries.updateWordInTable(tableName, rowID, baseEntry);
                        } catch (Exception e)
                        {
                            Toast.makeText(d_WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dataBaseQueries.insertWordInTable(new_table_name, baseEntry);
                    }
                    listViewSetSource(true);
                    switcher.showPrevious();
                }
                else
                {
                    new AlertDialog.Builder(d_WordEditor.this)
                            .setMessage(R.string.msg_wrong_text)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    }).create().show();
                }
            }
        });
    }

    private void checkMove_OnClick()
    {
        checkMove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkMove.isChecked())
                {
                    layoutSpinner.setVisibility(View.VISIBLE);
                }
                else
                {
                    layoutSpinner.setVisibility(View.GONE);
                }

//                if (checkMove.isChecked() && !editTextEn.getText().equals(null) && !editTextRu.getText().equals(null))
//                {
//                    buttonWrite.setEnabled(true);
//                }
//                else
//                {
//                    buttonWrite.setEnabled(false);
//                }
            }
        });
    }

    private void checkCopy_OnClick()
    {

    }





    private ArrayList<DataBaseEntry> getEntriesFromDB(String tableName)
    {
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        try
        {
            _databaseHelper.open();
            SQLiteDatabase database = _databaseHelper.database;
            database.beginTransaction();
            Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
            database.setTransactionSuccessful();
            database.endTransaction();
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
        } catch (Exception e)
        {
            Log.i("Lexicon", "Исключение в d_WordEditor.getEntriesFromDB() = " + e);
        }finally
        {
            _databaseHelper.close();
        }
        return entriesFromDB;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.d_word_editor_menu, menu);

//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.word_search).getActionView();
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.word_search:
                if (searchView.getVisibility() == View.GONE)
                {
                    searchView.setVisibility(View.VISIBLE);
                    searchIsVisible = true;
                }
                else if (searchView.getVisibility() == View.VISIBLE)
                {
                    searchView.setVisibility(View.GONE);
                    searchIsVisible = false;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchView_onListeners()
    {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                try
                {
                    //// TODO: 19.01.2017 Фильтрация ListView, вызов
                    d_WordEditor.this.listViewAdapter.getFilter().filter(newText);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            // Здесь будет храниться то, что пользователь ввёл в поисковой строке
            String search = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    private final int LOADER_GET_ENTRIES = 1;

    // TODO: AsyncTaskLoader - 1. WordEditor реализует интерфейс LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
    {
        Loader<Cursor> loader = null;
        switch (id)
        {
            case LOADER_GET_ENTRIES:
                loader = new GetEntriesLoader(this, bundle);
                break;
        }
        return loader;
    }

    @Override   // TODO: AsyncTaskLoader - 2. Реализация интерфейса LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (loader.getId() == LOADER_GET_ENTRIES)
        {
            loadDbEntryHandler(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }

    private void loadDbEntryHandler(Cursor cursor)
    {
        try
        {
            if (cursor != null && cursor.getCount() == 1)
            {
                if (cursor.moveToFirst())
                {
                    while ( !cursor.isAfterLast() )
                    {
                        editTextEn.setText(cursor.getString(0));
                        editTextRu.setText(cursor.getString(1));
                        spinnerCountRepeat.setSelection(Integer.parseInt(cursor.getString(3)));
                        cursor.moveToNext();
                    }
                }
            }
        } catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



}
