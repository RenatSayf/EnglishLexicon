package com.myapp.lexicon.wordeditor;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
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
import android.widget.SearchView;
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
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class WordEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String KEY_EXTRA_DICT_NAME = "wordeditor_dict_name";
    public static final String KEY_ROW_ID = "key_row_id";

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
    private ListViewAdapter listViewAdapter;
    private ProgressBar progressBar;
    private DataBaseQueries dataBaseQueries;
    private ViewSwitcher switcher;
    private LockOrientation lockOrientation;
    private String spinnerDictSelectItem;
    private WordEditorFields m;

    private final String KEY_SWITCHER_DISPLAYED_CHILD = "sw-d-ch";
    private final String KEY_SPINNER_SELECT_INDEX = "sp-slt-idx";
    private final String KEY_SPINNER_COUNT_REPEAT_SELECT_INDEX = "sp-cnt-rep-slt-idx";
    private final String KEY_SPINNER_ITEMS = "sp-items";
    private final String KEY_SPINNER_2_ITEMS = "sp-2-items";
    private final String KEY_SEARCH_QUERY = "srch-query";
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
        spinnerListDict =(Spinner)findViewById(R.id.spinner);

        searchView = (SearchView) findViewById(R.id.search_view);
        if (searchView != null)
        {
            searchView.setVisibility(View.GONE);
        }
        searchView_onListeners();

        listView=(ListView) findViewById(R.id.listView);

        progressBar= (ProgressBar) findViewById(R.id.progressBar);

        switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        Animation slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);
        switcher.setInAnimation(slide_in_left);
        switcher.setOutAnimation(slide_out_right);
        
        buttonWrite = (ImageButton) findViewById(R.id.btn_write);
        if (buttonWrite != null)
        {
            buttonWrite.setEnabled(true);
        }

        buttonDelete = (ImageButton) findViewById(R.id.btn_delete);

        buttonCancel = (ImageButton) findViewById(R.id.btn_cancel);

        editTextEn = (EditText) findViewById(R.id.edit_text_en);
        editTextRu = (EditText) findViewById(R.id.edit_text_ru);
        spinnerCountRepeat = (Spinner) findViewById(R.id.spinn_cout_repeat);
        spinnerListDict2 = (Spinner) findViewById(R.id.spinn_dict_to_move);

        checkCopy = (CheckBox) findViewById(R.id.check_copy);

        checkMove = (CheckBox) findViewById(R.id.check_move);

        layoutSpinner = (LinearLayout) findViewById(R.id.lin_layout_spin);
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
        outState.putParcelable(KEY_FIELDS, m);

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

        ArrayList<String> spinner2Items = new ArrayList<>();
        for (int i = 0; i < spinnerListDict2.getCount(); i++)
        {
            spinner2Items.add(spinnerListDict2.getItemAtPosition(i).toString());
        }
        outState.putStringArrayList(KEY_SPINNER_2_ITEMS, spinner2Items);

        outState.putString(KEY_EDITTEXT_EN, editTextEn.getText().toString());
        outState.putString(KEY_EDITTEXT_RU, editTextRu.getText().toString());
        outState.putBoolean(KEY_CHECK_COPY, checkCopy.isChecked());
        outState.putBoolean(KEY_CHECK_MOVE, checkMove.isChecked());
        AppData2.getInstance().setListViewAdapter((ListViewAdapter) listView.getAdapter());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_layout_word_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
            if (m != null && m.searchIsVisible[0])
            {
                searchView.setVisibility(View.VISIBLE);
                searchView.setQuery(savedInstanceState.getString(KEY_SEARCH_QUERY), false);
            }

            switcher.setDisplayedChild(savedInstanceState.getInt(KEY_SWITCHER_DISPLAYED_CHILD));
            ArrayList<String> arrayList = savedInstanceState.getStringArrayList(KEY_SPINNER_ITEMS);
            if (arrayList != null)
            {
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(this, R.layout.my_content_spinner_layout, arrayList);
                spinnerListDict.setAdapter(adapterSpinner);
                int index = savedInstanceState.getInt(KEY_SPINNER_SELECT_INDEX);
                if (index < adapterSpinner.getCount())
                {
                    spinnerListDict.setSelection(savedInstanceState.getInt(KEY_SPINNER_SELECT_INDEX));
                }
            }

            ArrayList<String> arrayList2 = savedInstanceState.getStringArrayList(KEY_SPINNER_2_ITEMS);
            if (arrayList2 != null)
            {
                ArrayAdapter<String> adapterSpinner2= new ArrayAdapter<>(this, R.layout.my_content_spinner_layout, arrayList2);
                spinnerListDict2.setAdapter(adapterSpinner2);
            }

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
            getLoaderManager().restartLoader(LOADER_GET_TABLE_LIST, null, WordEditor.this).forceLoad();
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
            Bundle bundle = new Bundle();
            bundle.putString(GetAllFromTableLoader.KEY_TABLE_NAME, spinnerListDict.getSelectedItem().toString());
            getLoaderManager().restartLoader(LOADER_GET_ALL_FROM_TABLE, bundle, WordEditor.this).forceLoad();
        }
        else
        {
            listView.setAdapter(AppData2.getInstance().getListViewAdapter());
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
                TextView textViewEn = (TextView) view.findViewById(R.id.english);
                m.oldTextEn = textViewEn.getText().toString();
                editTextEn.setText(textViewEn.getText().toString());

                TextView textViewRu = (TextView) view.findViewById(R.id.translate);
                m.oldTextRu = textViewRu.getText().toString();
                editTextRu.setText(textViewRu.getText().toString());

                TextView textViewCounRepeat = (TextView) view.findViewById(R.id.count_repeat);
                try
                {
                    m.oldCountRepeat = Integer.parseInt(textViewCounRepeat.getText().toString());
                } catch (NumberFormatException e)
                {
                    m.oldCountRepeat = 1;
                }
                spinnerCountRepeat.setSelection(m.oldCountRepeat);
                m.oldCurrentDict = spinnerListDict.getSelectedItem().toString();

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
                final String tableName = spinnerListDict.getSelectedItem().toString();

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
                                    dataBaseQueries.deleteWordInTableSync(tableName, m.rowID);
                                    dataBaseQueries.dataBaseVacuum(tableName);
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

                if (editTextEn.getText().toString().equals(m.oldTextEn) && editTextRu.getText().toString().equals(m.oldTextRu) && Integer.parseInt(spinnerCountRepeat.getSelectedItem().toString()) == m.oldCountRepeat && spinnerListDict2.getSelectedItem().toString().equals(m.oldCurrentDict))
                {
                    return;
                }

                StringOperations stringOperations = StringOperations.getInstance();
                if (stringOperations.getLangOfText(editTextEn.getText().toString())[1].equals("en") &&
                        stringOperations.getLangOfText(editTextRu.getText().toString())[1].equals("ru"))
                {
                    String tableName = spinnerListDict.getSelectedItem().toString();
                    String new_table_name = spinnerListDict2.getSelectedItem().toString();
                    DataBaseEntry baseEntry = new DataBaseEntry(editTextEn.getText().toString(), editTextRu.getText().toString(), spinnerCountRepeat.getSelectedItem().toString());
                    if (!checkMove.isChecked())
                    {
                        try
                        {
                            dataBaseQueries.updateWordInTableSync(tableName, m.rowID, baseEntry);
                        } catch (Exception e)
                        {
                            Toast.makeText(WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (checkMove.isChecked() && !checkCopy.isChecked())
                    {
                        try
                        {
                            dataBaseQueries.deleteWordInTableSync(tableName, m.rowID);
                            dataBaseQueries.dataBaseVacuum(tableName);
                        } catch (Exception e)
                        {
                            Toast.makeText(WordEditor.this, getString(R.string.msg_data_base_error)+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.d_word_editor_menu, menu);
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
                    m.searchIsVisible[0] = true;
                }
                else if (searchView.getVisibility() == View.VISIBLE)
                {
                    searchView.setVisibility(View.GONE);
                    m.searchIsVisible[0] = false;
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
                listViewAdapter = new ListViewAdapter(entriesFromDB, WordEditor.this, R.id.search_view);
                listView.setAdapter(listViewAdapter); // TODO: ListView setAdapter
                progressBar.setVisibility(View.GONE);

                if (getIntent().getExtras().containsKey(WordEditor.KEY_ROW_ID))
                {
                    int index = getIntent().getExtras().getInt(WordEditor.KEY_ROW_ID);
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
            }
        }
        catch (Exception e)
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
        ArrayList<String> list = new ArrayList<>();
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
                            list.add( table_name );
                        }
                        cursor.moveToNext();
                    }
                }
            }
            if (list.size() > 0)
            {
                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, R.layout.my_content_spinner_layout, list);
                spinnerListDict.setAdapter(adapterSpinner);
                int position;
                if (spinnerDictSelectItem != null)
                {
                    position = adapterSpinner.getPosition(spinnerDictSelectItem);
                } else
                {
                    try
                    {
                        position = adapterSpinner.getPosition(getIntent().getExtras().getString(WordEditor.KEY_EXTRA_DICT_NAME));
                    } catch (Exception e)
                    {
                        position = 0;
                    }
                }
                spinnerListDict.setSelection(position);

                ArrayList<String> list2 = (ArrayList<String>) list.clone();
                list2.remove(position);
                ArrayAdapter<String> adapterSpinner2 = new ArrayAdapter<>(this, R.layout.my_content_spinner_layout, list2);
                spinnerListDict2.setAdapter(adapterSpinner2);
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

    public void btnSpeak_OnClick(View view)
    {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "word_editor_btn_speak_onclick");
        SplashScreenActivity.speech.setLanguage(Locale.US);
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
