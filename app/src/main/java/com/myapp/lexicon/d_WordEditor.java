package com.myapp.lexicon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class d_WordEditor extends AppCompatActivity
{
    private Spinner spinnerListDict;
    private ListView listView;
    private ImageButton buttonWrite;
    private ImageButton buttonDelete;
    private ImageButton buttonCancel;
    private EditText editTextEn, editTextRu;
    private Spinner spinnerCountRepeat, spinnerListDict2;
    private CheckBox checkCopy, checkMove;
    private LinearLayout layoutSpinner;
    private d_ListViewAdapter lictViewAdapter;
    private ArrayList<DataBaseEntry> dataBaseEntries;
    private Handler handler;
    private ProgressBar progressBar;
    private DatabaseHelper _databaseHelper;
    private DataBaseQueries dataBaseQueries;
    private ViewSwitcher switcher;
    private Animation slide_in_left, slide_out_right;
    

    private void initViews()
    {
        spinnerListDict =(Spinner)findViewById(R.id.spinner);
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
        buttonWrite.setEnabled(false);

        buttonDelete = (ImageButton) findViewById(R.id.btn_delete);

        buttonCancel = (ImageButton) findViewById(R.id.btn_cancel);

        editTextEn = (EditText) findViewById(R.id.edit_text_en);
        editTextRu = (EditText) findViewById(R.id.edit_text_ru);
        spinnerCountRepeat = (Spinner) findViewById(R.id.spinn_cout_repeat);
        spinnerListDict2 = (Spinner) findViewById(R.id.spinn_list_dict);

        checkCopy = (CheckBox) findViewById(R.id.check_copy);

        checkMove = (CheckBox) findViewById(R.id.check_move);

        layoutSpinner = (LinearLayout) findViewById(R.id.lin_layout_spin);
        layoutSpinner.setVisibility(View.GONE);

        spinner_OnItemSelected();
        listView_OnItemClick();
        editTextEn_OnTextChanged();
        editTextRu_OnTextChanged();
        buttonWrite_OnClick();
        buttonDelete_OnClick();
        buttonCancel_OnClick();
        checkCopy_OnClick();
        checkMove_OnClick();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i("Lexicon", "Вход в d_WordEditor.onCreate");
        setContentView(R.layout.d_layout_word_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (_databaseHelper == null)
        {
            _databaseHelper = new DatabaseHelper(this);
            _databaseHelper.create_db();
        }

        try
        {
            dataBaseQueries = new DataBaseQueries(this);
        } catch (SQLException e)
        {
            e.printStackTrace();
            z_Log.v("Исключение - "+e.getMessage());
        }
        initViews();
        dataBaseQueries.setListTableToSpinner(spinnerListDict,0);
        dataBaseQueries.setListTableToSpinner(spinnerListDict2,0);

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
                progressBar.setVisibility(View.VISIBLE);
                listViewSetSource();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });

        spinnerListDict2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        spinnerCountRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                z_Log.v("spinnerCountRepeat position = "+position);
                if (position != testCountRepeat-1 && !editTextEn.getText().equals(null) && !editTextRu.getText().equals(null))
                {
                    buttonWrite.setEnabled(true);
                }
                else
                {
                    buttonWrite.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void listViewSetSource()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                String selectedItem = spinnerListDict.getSelectedItem().toString();
                z_Log.v("Вход в spinnerListDict.setOnItemSelectedListener = " + selectedItem);
                dataBaseEntries = getEntriesFromDB(selectedItem);

                lictViewAdapter = new d_ListViewAdapter(dataBaseEntries, d_WordEditor.this);
                handler.sendEmptyMessage(0);
            }
        }).start();

        handler=new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                listView.setAdapter(lictViewAdapter);
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    private long rowID;
    private String testTextEn;
    private String testTextRu;
    private int testCountRepeat;
    private long listViewSelectItem;
    private void listView_OnItemClick()
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                listViewSelectItem = id;
                TextView textViewEn = (TextView) view.findViewById(R.id.english);
                testTextEn = textViewEn.getText().toString();
                editTextEn.setText(textViewEn.getText().toString());

                TextView textViewRu = (TextView) view.findViewById(R.id.translate);
                testTextRu = textViewRu.getText().toString();
                editTextRu.setText(textViewRu.getText().toString());
                String tableName = spinnerListDict.getSelectedItem().toString();
                z_Log.v("spinnerListDict.getSelectedItem() = "+tableName);
                try
                {
                    rowID = dataBaseQueries.getIdOfWord(tableName, testTextEn, testTextRu);
                    testCountRepeat = Integer.parseInt(dataBaseEntries.get(position).get_count_repeat());
                } catch (Exception e)
                {
                    testCountRepeat = 1;
                    z_Log.v("Исключение - "+e.getMessage());
                }
                spinnerCountRepeat.setSelection(testCountRepeat - 1);
                checkMove.setChecked(false);
                layoutSpinner.setVisibility(View.GONE);


                z_Log.v("rowID = "+rowID);
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


    private void editTextRu_OnTextChanged()
    {
        editTextEn.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (editTextEn.getText().toString().equals("") || editTextRu.getText().toString().equals(""))
                {
                    buttonWrite.setEnabled(false);
                }else
                {
                    buttonWrite.setEnabled(true);
                }
                if (editTextEn.getText().toString().equals(testTextEn))
                {
                    buttonWrite.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }

    private void editTextEn_OnTextChanged()
    {
        editTextRu.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (editTextEn.getText().toString().equals("") || editTextRu.getText().toString().equals(""))
                {
                    buttonWrite.setEnabled(false);
                }else
                {
                    buttonWrite.setEnabled(true);
                }
                if (editTextRu.getText().toString().equals(testTextRu))
                {
                    buttonWrite.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

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
                String tableName = spinnerListDict.getSelectedItem().toString();
                try
                {
                    dataBaseQueries.deleteWordInTable(tableName, rowID);
                    dataBaseQueries.dataBaseVacuum(tableName);
                } catch (Exception e)
                {
                    z_Log.v("Возникло исключение - "+e.getMessage());
                }
                listViewSetSource();
                switcher.showPrevious();
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
                z_StringOperations stringOperations = z_StringOperations.getInstance();
                if (stringOperations.getLangOfText(editTextEn.getText().toString())[1].equals("en") &&
                        stringOperations.getLangOfText(editTextRu.getText().toString())[1].equals("ru"))
                {
                    String tableName = spinnerListDict.getSelectedItem().toString();
                    String new_table_name = spinnerListDict2.getSelectedItem().toString();
                    String old_en = testTextEn;
                    String old_ru = testTextRu;
                    DataBaseEntry baseEntry = new DataBaseEntry(editTextEn.getText().toString(), editTextRu.getText().toString(), null, spinnerCountRepeat.getSelectedItem().toString());
                    if (!checkMove.isChecked())
                    {
                        try
                        {
                            dataBaseQueries.updateWordInTable(tableName, rowID, baseEntry);
                        } catch (Exception e)
                        {
                            z_Log.v("Возникло исключение - "+e.getMessage());
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
                            z_Log.v("Возникло исключение - "+e.getMessage());
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
                            z_Log.v("Возникло исключение - "+e.getMessage());
                        }
                        dataBaseQueries.insertWordInTable(new_table_name, baseEntry);
                    }
                    listViewSetSource();
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

                if (checkMove.isChecked() && !editTextEn.getText().equals(null) && !editTextRu.getText().equals(null))
                {
                    buttonWrite.setEnabled(true);
                }
                else
                {
                    buttonWrite.setEnabled(false);
                }
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
                    DataBaseEntry dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1));
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
            case R.id.action_item1:
                break;
            case R.id.action_item2:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    
    

}
