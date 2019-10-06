package com.myapp.lexicon.wordeditor;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.support.v7.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetAllFromTableLoader;
import com.myapp.lexicon.database.GetEntriesLoader;
import com.myapp.lexicon.database.GetTableListLoader;
import com.myapp.lexicon.database.UpdateDBEntryAsync;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class WordEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String KEY_EXTRA_DICT_NAME = "wordeditor_dict_name";
    public static final String KEY_ROW_ID = "key_row_id";

    private Spinner dictListSpinner;
    private int spinner_select_pos = -1;
    private SearchView searchView;
    private ListView listView;
    private ImageButton buttonWrite;
    private ImageButton buttonDelete;
    private ImageButton buttonCancel;
    private EditText editTextEn, editTextRu;
    private TextView tvAmountWords;
    private Spinner spinnerCountRepeat, spinnerDictToMove;
    private CheckBox checkCopy, checkMove;
    private LinearLayout layoutSpinner;
    private ListViewAdapter listViewAdapter;
    private ProgressBar progressBar;
    private DataBaseQueries dataBaseQueries;
    private ViewSwitcher switcher;
    private LockOrientation lockOrientation;
    private WordEditorFields m;

    private final String KEY_SWITCHER_DISPLAYED_CHILD = "sw-d-ch";
    private final String KEY_SPINNER_SELECT_INDEX = "sp-slt-idx";
    private final String KEY_SPINNER_COUNT_REPEAT_SELECT_INDEX = "sp-cnt-rep-slt-idx";
    private final String KEY_SPINNER_ITEMS = "sp-items";
    private final String KEY_SPINNER_2_ITEMS = "sp-2-items";
    private final String KEY_EDITTEXT_EN = "edit-txt-en";
    private final String KEY_EDITTEXT_RU = "edit-txt-ru";
    private final String KEY_CHECK_COPY = "check-copy";
    private final String KEY_CHECK_MOVE = "check-move";
    private final String KEY_FIELDS = "key_fields";

    private final int LOADER_GET_ENTRIES = 1;
    private final int LOADER_GET_TABLE_LIST = 2;
    private final int LOADER_GET_ALL_FROM_TABLE = 3;

    private void initViews()
    {
        dictListSpinner = findViewById(R.id.spinner);

        listView = findViewById(R.id.listView);

        progressBar= findViewById(R.id.progressBar);

        switcher = findViewById(R.id.viewSwitcher);
        Animation slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);
        switcher.setInAnimation(slide_in_left);
        switcher.setOutAnimation(slide_out_right);
        
        buttonWrite = findViewById(R.id.btn_write);
        if (buttonWrite != null)
        {
            buttonWrite.setEnabled(true);
        }

        buttonDelete = findViewById(R.id.btn_delete);

        buttonCancel = findViewById(R.id.btn_cancel);

        tvAmountWords = findViewById(R.id.tv_amount_words);
        editTextEn = findViewById(R.id.edit_text_en);
        editTextRu = findViewById(R.id.edit_text_ru);
        spinnerCountRepeat = findViewById(R.id.spinn_cout_repeat);
        spinnerDictToMove = findViewById(R.id.spinn_dict_to_move);

        checkCopy = findViewById(R.id.check_copy);

        checkMove = findViewById(R.id.check_move);

        layoutSpinner = findViewById(R.id.lin_layout_spin);
        if (layoutSpinner != null)
        {
            layoutSpinner.setVisibility(View.GONE);
        }

        spinner_OnItemSelected();
        listView_OnItemClick();
        buttonWrite_OnClick();
        buttonDelete_OnClick();
        buttonCancel_OnClick();
        checkMove_OnClick();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        m.searchIsVisible[0] = searchView.isIconified();
        m.queryString = searchView.getQuery().toString();
        outState.putParcelable(KEY_FIELDS, m);

        outState.putInt(KEY_SWITCHER_DISPLAYED_CHILD, switcher.getDisplayedChild());
        outState.putInt(KEY_SPINNER_SELECT_INDEX, dictListSpinner.getSelectedItemPosition());
        outState.putInt(KEY_SPINNER_COUNT_REPEAT_SELECT_INDEX, spinnerCountRepeat.getSelectedItemPosition());
//        String KEY_SEARCH_QUERY = "srch-query";
//        outState.putString(KEY_SEARCH_QUERY, searchView.getQuery().toString());

        ArrayList<String> spinnerItems = new ArrayList<>();
        for (int i = 0; i < dictListSpinner.getCount(); i++)
        {
            spinnerItems.add(dictListSpinner.getItemAtPosition(i).toString());
        }
        outState.putStringArrayList(KEY_SPINNER_ITEMS, spinnerItems);

        ArrayList<String> spinner2Items = new ArrayList<>();
        for (int i = 0; i < spinnerDictToMove.getCount(); i++)
        {
            spinner2Items.add(spinnerDictToMove.getItemAtPosition(i).toString());
        }
        outState.putStringArrayList(KEY_SPINNER_2_ITEMS, spinner2Items);

        outState.putString(KEY_EDITTEXT_EN, editTextEn.getText().toString());
        outState.putString(KEY_EDITTEXT_RU, editTextRu.getText().toString());
        outState.putBoolean(KEY_CHECK_COPY, checkCopy.isChecked());
        outState.putBoolean(KEY_CHECK_MOVE, checkMove.isChecked());
        AppData.getInstance().setListViewAdapter((ListViewAdapter) listView.getAdapter());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_layout_word_editor);
        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        lockOrientation = new LockOrientation(this);
        dataBaseQueries = new DataBaseQueries(this);

        initViews();

        if (savedInstanceState == null)
        {
            m = new WordEditorFields();
            spinnerCountRepeat.setSelection(1);
        }

        // TODO: AsyncTaskLoader - 3. инициализация
        getLoaderManager().initLoader(LOADER_GET_ENTRIES, savedInstanceState, this);

        if (savedInstanceState != null)
        {
            m = savedInstanceState.getParcelable(KEY_FIELDS);

            switcher.setDisplayedChild(savedInstanceState.getInt(KEY_SWITCHER_DISPLAYED_CHILD));
            ArrayList<String> arrayList = savedInstanceState.getStringArrayList(KEY_SPINNER_ITEMS);
            if (arrayList != null)
            {
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
                dictListSpinner.setAdapter(adapterSpinner);
                int index = savedInstanceState.getInt(KEY_SPINNER_SELECT_INDEX);
                if (index < adapterSpinner.getCount())
                {
                    dictListSpinner.setSelection(savedInstanceState.getInt(KEY_SPINNER_SELECT_INDEX));
                }
            }

            ArrayList<String> arrayList2 = savedInstanceState.getStringArrayList(KEY_SPINNER_2_ITEMS);
            if (arrayList2 != null)
            {
                ArrayAdapter<String> adapterSpinner2= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList2);
                spinnerDictToMove.setAdapter(adapterSpinner2);
            }

            spinnerCountRepeat.setSelection(savedInstanceState.getInt(KEY_SPINNER_COUNT_REPEAT_SELECT_INDEX));
            spinner_select_pos = dictListSpinner.getSelectedItemPosition();
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
            String text = getString(R.string.text_words) + "  " + m.amountWords;
            tvAmountWords.setText(text);
        }
        else
        {
            getLoaderManager().restartLoader(LOADER_GET_TABLE_LIST, null, WordEditor.this).forceLoad();
        }


        AppData appData = AppData.getInstance();
        if (appData.isAdMob())
        {
            if (appData.isOnline(this))
            {
                BottomBannerFragmentWE bannerFragment = (BottomBannerFragmentWE) getSupportFragmentManager().findFragmentByTag(BottomBannerFragmentWE.TAG);
                if (bannerFragment == null)
                {
                    bannerFragment = new BottomBannerFragmentWE();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.bottom_banner_frame_we, bannerFragment).commit();
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
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
        dictListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
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
            Bundle bundle = new Bundle();
            bundle.putString(GetAllFromTableLoader.KEY_TABLE_NAME, dictListSpinner.getSelectedItem().toString());
            getLoaderManager().restartLoader(LOADER_GET_ALL_FROM_TABLE, bundle, WordEditor.this).forceLoad();
        }
        else
        {
            listView.setAdapter(AppData.getInstance().getListViewAdapter());
            progressBar.setVisibility(View.GONE);
        }
    }

    private void listView_OnItemClick()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                m.rowID = position + 1;
                TextView textViewEn = view.findViewById(R.id.english);
                m.oldTextEn = textViewEn.getText().toString();
                editTextEn.setText(textViewEn.getText().toString());

                TextView textViewRu = view.findViewById(R.id.translate);
                m.oldTextRu = textViewRu.getText().toString();
                editTextRu.setText(textViewRu.getText().toString());

                TextView textViewCounRepeat = view.findViewById(R.id.count_repeat);
                try
                {
                    m.oldCountRepeat = Integer.parseInt(textViewCounRepeat.getText().toString());
                } catch (NumberFormatException e)
                {
                    m.oldCountRepeat = 1;
                }

                ArrayList<String> list2 = new ArrayList<>(m.dictNames);
                list2.remove(dictListSpinner.getSelectedItem().toString());
                ArrayAdapter<String> adapterSpinner2 = new ArrayAdapter<>(WordEditor.this, android.R.layout.simple_list_item_1, list2);
                spinnerDictToMove.setAdapter(adapterSpinner2);

                spinnerCountRepeat.setSelection(m.oldCountRepeat);
                m.oldCurrentDict = dictListSpinner.getSelectedItem().toString();

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
                final String tableName = dictListSpinner.getSelectedItem().toString();

                new AlertDialog.Builder(WordEditor.this) // TODO: AlertDialog с макетом по умолчанию
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
                                    dataBaseQueries.deleteWordInTableSync(tableName, editTextEn.getText().toString(), editTextRu.getText().toString());
                                    dataBaseQueries.dataBaseVacuum(tableName);
                                    listViewSetSource(true);
                                } catch (Exception e)
                                {
                                    Toast.makeText(WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    WordEditor.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                }
                                listViewSetSource(true);
                                switcher.showPrevious();
                                lockOrientation.unLock();
                            }
                        })
                        .setNegativeButton(R.string.button_text_no, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                lockOrientation.unLock();
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

                try
                {
                    if (editTextEn.getText().toString().equals(m.oldTextEn) && editTextRu.getText().toString().equals(m.oldTextRu) && Integer.parseInt(spinnerCountRepeat.getSelectedItem().toString()) == m.oldCountRepeat && spinnerDictToMove.getSelectedItem().toString().equals(m.oldCurrentDict))
                    {
                        return;
                    }

                    StringOperations stringOperations = StringOperations.getInstance();
                    if (stringOperations.getLangOfText(WordEditor.this, editTextEn.getText().toString())[1].equals("en") && !stringOperations.getLangOfText(WordEditor.this, editTextRu.getText().toString())[1].equals("en"))
                    {
                        String tableName = dictListSpinner.getSelectedItem().toString();
                        String new_table_name = spinnerDictToMove.getSelectedItem().toString();
                        DataBaseEntry baseEntry = new DataBaseEntry(editTextEn.getText().toString(), editTextRu.getText().toString(), spinnerCountRepeat.getSelectedItem().toString());
                        if (!checkMove.isChecked())
                        {
                            long res = dataBaseQueries.updateWordInTableSync(tableName, m.rowID, baseEntry);
                            if (res >= 0)
                            {
                                Toast.makeText(WordEditor.this, R.string.text_updated_successfully, Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(WordEditor.this, R.string.text_write_error,Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            checkMove.isChecked();
                            if (!checkCopy.isChecked())
                            {
                                long res = dataBaseQueries.deleteWordInTableSync(tableName, editTextEn.getText().toString(), editTextRu.getText().toString());
                                dataBaseQueries.dataBaseVacuum(tableName);
                                if (res >= 0)
                                {
                                    Toast.makeText(WordEditor.this, R.string.text_updated_successfully, Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(WordEditor.this, R.string.text_deleting_error,Toast.LENGTH_SHORT).show();
                                }
                                dataBaseQueries.insertWordInTableSync(new_table_name, baseEntry);
                            }
                            else if (checkMove.isChecked() && checkCopy.isChecked())
                            {
                                try
                                {
                                    dataBaseQueries.updateWordInTableSync(tableName, m.rowID, baseEntry);
                                } catch (Exception e)
                                {
                                    Toast.makeText(WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                dataBaseQueries.insertWordInTableSync(new_table_name, baseEntry);
                            }
                        }
                        listViewSetSource(true);
                        switcher.showPrevious();
                    }
                    else
                    {
                        new AlertDialog.Builder(WordEditor.this)
                                .setMessage(R.string.msg_wrong_text)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {

                                    }
                                }).create().show();
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    new AlertDialog.Builder(WordEditor.this)
                            .setMessage(R.string.msg_wrong_text)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
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
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.d_word_editor_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.word_search);
        searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null)
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView_onListeners(searchView);
        if (m != null && m.searchIsVisible.length > 0)
        {
            searchView.setIconified(m.searchIsVisible[0]);
            searchView.setQuery(m.queryString, false);
            searchView.setOnSearchClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    searchView.setQuery(m.queryString, false);
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.do_repeat)
        {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_Count_REPEAT, 1);
            String dictName = dictListSpinner.getSelectedItem().toString();
            UpdateDBEntryAsync updateDBEntryAsync = new UpdateDBEntryAsync(this, dictName, values, null, null, new UpdateDBEntryAsync.IUpdateDBListener()
            {
                @Override
                public void updateDBEntry_OnComplete(int rows)
                {
                    listViewSetSource(true);
                }
            });
            if (updateDBEntryAsync.getStatus() != AsyncTask.Status.RUNNING)
            {
                updateDBEntryAsync.execute();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchView_onListeners(SearchView searchView)
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
                    //// TODO: Фильтрация ListView, вызов
                    listViewAdapter = (ListViewAdapter) listView.getAdapter();
                    listViewAdapter.getFilter().filter(newText);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }


//    @Override
//    protected void onNewIntent(Intent intent)
//    {
//        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
//        {
//            // Здесь будет храниться то, что пользователь ввёл в поисковой строке
//            String search = intent.getStringExtra(SearchManager.QUERY);
//        }
//    }

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
            case LOADER_GET_TABLE_LIST:
                loader = new GetTableListLoader(this);
                break;
            case LOADER_GET_ALL_FROM_TABLE:
                loader = new GetAllFromTableLoader(this, bundle);
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
        if (loader.getId() == LOADER_GET_TABLE_LIST)
        {
            loadDbTableListHandler(data);
        }
        if (loader.getId() == LOADER_GET_ALL_FROM_TABLE)
        {
            loadDbAllHandler(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }

    private void loadDbAllHandler(Cursor cursor)
    {
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        try
        {
            if (cursor != null && cursor.getCount() > 0)
            {
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        DataBaseEntry dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(3));
                        entriesFromDB.add(dataBaseEntry);
                        cursor.moveToNext();
                    }
                }
                listViewAdapter = new ListViewAdapter(entriesFromDB, WordEditor.this, R.id.word_search);
                listView.setAdapter(listViewAdapter); // TODO: ListView setAdapter
                progressBar.setVisibility(View.GONE);
                m.amountWords = entriesFromDB.size();
                String text = getString(R.string.text_words) + "  " + m.amountWords;
                tvAmountWords.setText(text);

                if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(WordEditor.KEY_ROW_ID))
                {
                    int index = getIntent().getExtras().getInt(WordEditor.KEY_ROW_ID);
                    m.rowID = index;
                    DataBaseEntry entry = listViewAdapter.getItem(index-1);
                    if (entry != null)
                    {
                        editTextEn.setText(entry.getEnglish());
                        editTextRu.setText(entry.getTranslate());
                        spinnerCountRepeat.setSelection(Integer.parseInt(entry.getCountRepeat()));
                        switcher.showNext();
                    }

                    getIntent().removeExtra(WordEditor.KEY_ROW_ID);
                }
            }
            else
            {
                listView.setAdapter(null);
                progressBar.setVisibility(View.GONE);
                m.amountWords = 0;
                String text = getString(R.string.text_words) + "  " + m.amountWords;
                tvAmountWords.setText(text);
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
        }
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
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
    }

    private void loadDbTableListHandler(Cursor cursor)
    {
        String nameNotDict;

        m.dictNames.clear();
        try
        {
            if (cursor != null && cursor.getCount() > 0)
            {
                if (cursor.moveToFirst())
                {
                    while ( !cursor.isAfterLast() )
                    {
                        nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                        if (!nameNotDict.equals(DatabaseHelper.TABLE_METADATA) && !nameNotDict.equals(DatabaseHelper.TABLE_SEQUENCE) && !nameNotDict.equals(DatabaseHelper.TABLE_API_KEY))
                        {
                            String table_name = cursor.getString(cursor.getColumnIndex("name"));
                            table_name = StringOperations.getInstance().underscoreToSpace(table_name);
                            m.dictNames.add( table_name );
                        }
                        cursor.moveToNext();
                    }
                }
            }
            if (m.dictNames.size() > 0)
            {
                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, m.dictNames);
                dictListSpinner.setAdapter(adapterSpinner);
                int position = 0;
//                if (spinnerDictSelectItem != null)
//                {
//                    position = adapterSpinner.getPosition(spinnerDictSelectItem);
//                } else
//                {
//                    try
//                    {
//                        position = adapterSpinner.getPosition(getIntent().getExtras().getString(WordEditor.KEY_EXTRA_DICT_NAME));
//                    } catch (Exception e)
//                    {
//                        position = 0;
//                    }
//                }

                try
                {
                    if (getIntent().getExtras() != null)
                    {
                        position = adapterSpinner.getPosition(getIntent().getExtras().getString(WordEditor.KEY_EXTRA_DICT_NAME));
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                dictListSpinner.setSelection(position);

                ArrayList<String> list2 = new ArrayList<>(m.dictNames);
                list2.remove(dictListSpinner.getSelectedItem().toString());
                ArrayAdapter<String> adapterSpinner2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);
                spinnerDictToMove.setAdapter(adapterSpinner2);
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
        }
    }

    public void btn_Speak_OnClick(View view)
    {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "word_editor_btn_speak_onclick");
        try
        {
            SplashScreenActivity.speech.setLanguage(Locale.US);
        } catch (Exception e)
        {
            return;
        }
        SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
        if (!editTextEn.getText().toString().equals(""))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                SplashScreenActivity.speech.speak(editTextEn.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
            } else
            {
                SplashScreenActivity.speech.speak(editTextEn.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
            }
        }
    }
}
