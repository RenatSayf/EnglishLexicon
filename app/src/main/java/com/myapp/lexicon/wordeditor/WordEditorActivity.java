package com.myapp.lexicon.wordeditor;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.myapp.lexicon.R;
import com.myapp.lexicon.addword.AddWordViewModel;
import com.myapp.lexicon.ads.AdsExtensionsKt;
import com.myapp.lexicon.dialogs.ConfirmDialog;
import com.myapp.lexicon.helpers.AppBus;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.main.SpeechViewModel;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.viewmodels.EditorSearchViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;


@SuppressWarnings("CodeBlock2Expr")
@AndroidEntryPoint
public class WordEditorActivity extends AppCompatActivity implements ListViewAdapter.IListViewAdapter
{
    public static final String KEY_EXTRA_DICT_NAME = "wordeditor_dict_name";
    public static final String KEY_EXTRA_EN_WORD = "KEY_EXTRA_EN_WORD";
    public static final String KEY_EXTRA_RU_WORD = "KEY_EXTRA_RU_WORD";
    public static final int requestCode = 2654789;

    private Spinner dictListSpinner;
    private SearchView searchView;
    private ListView listView;
    private ImageButton buttonWrite;
    private ImageButton buttonDelete;
    private ImageButton buttonCancel;
    private EditText editTextEn, editTextRu;
    private TextView tvAmountWords;
    private Spinner spinnerDictToMove;
    private CheckBox checkCopy, checkMove;
    private LinearLayout layoutSpinner;
    private ListViewAdapter listViewAdapter;
    private ProgressBar progressBar;
    private ViewSwitcher switcher;

    private MainViewModel mainVM;
    private EditorViewModel editorVM;
    private AddWordViewModel addWordVM;
    private SpeechViewModel spechVM;

    private void initViews()
    {
        dictListSpinner = findViewById(R.id.spinner);
        listView = findViewById(R.id.listView);
        progressBar= findViewById(R.id.speechProgress);
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
        buttonCancel = findViewById(R.id.btn_cancel);
        tvAmountWords = findViewById(R.id.tv_amount_words);
        editTextEn = findViewById(R.id.edit_text_en);
        editTextRu = findViewById(R.id.edit_text_ru);
        spinnerDictToMove = findViewById(R.id.spinn_dict_to_move);

        checkCopy = findViewById(R.id.check_copy);

        checkMove = findViewById(R.id.check_move);

        layoutSpinner = findViewById(R.id.lin_layout_spin);
        if (layoutSpinner != null)
        {
            layoutSpinner.setVisibility(View.INVISIBLE);
        }

        buttonWrite_OnClick();
        buttonCancel_OnClick();
        checkMove_OnClick();
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_layout_word_editor);
        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        toolbar.setTitleTextColor(getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        editorVM = new ViewModelProvider(this).get(EditorViewModel.class);
        addWordVM = new ViewModelProvider(WordEditorActivity.this).get(AddWordViewModel.class);
        spechVM = new ViewModelProvider(this).get(SpeechViewModel.class);

        initViews();

        mainVM.getDictionaryList().observe(this, dicts -> {
            if (!dicts.isEmpty())
            {
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dicts);
                dictListSpinner.setAdapter(adapterSpinner);
                String currentDict = getIntent().getStringExtra(KEY_EXTRA_DICT_NAME);
                if (currentDict != null)
                {
                    int index = dicts.indexOf(currentDict);
                    if (index >= 0)
                    {
                        dictListSpinner.setSelection(index);
                    }
                    List<String> subList = dicts.subList(0, dicts.size());
                    editorVM.setDictsToMove(subList);
                }
            }
        });

        editorVM.getDictsToMove().observe(this, dicts -> {
            if (!dicts.isEmpty())
            {
                String selectedItem = dictListSpinner.getSelectedItem().toString();
                ArrayList<String> list = new ArrayList<>();
                for (String i : dicts)
                {
                    if (!i.equals(selectedItem))
                    {
                        list.add(i);
                    }
                }
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
                spinnerDictToMove.setAdapter(adapterSpinner);
            }
        });

        editorVM.isMoveWord().observe(this, isMove -> {
            if (isMove != null && checkMove != null)
            {
                checkMove.setChecked(isMove);
                if (isMove)
                {
                    layoutSpinner.setVisibility(View.VISIBLE);
                }
                else
                {
                    layoutSpinner.setVisibility(View.INVISIBLE);
                }
            }
        });

        editorVM.wordsList.observe(this, words -> {
            listViewAdapter = new ListViewAdapter((ArrayList<Word>) words, this);
            listView.setAdapter(listViewAdapter); // Hint: ListView setAdapter
            progressBar.setVisibility(View.GONE);
            String text = getString(R.string.text_words) + "  " + words.size();
            tvAmountWords.setText(text);
        });

        buttonDelete = findViewById(R.id.btn_delete);
        buttonDelete_OnClick();
        editorVM.getDeletedId().observe(this, id -> {
            if (id > 0)
            {
                if (editorVM.selectedWord != null)
                {
                    Toast.makeText(this, "Слово удалено", Toast.LENGTH_SHORT).show();
                    editorVM.getAllWordsByDictName(editorVM.selectedWord.getDictName());
                }
            }
            else if (id < 0)
            {
                Toast.makeText(WordEditorActivity.this, getString(R.string.msg_data_base_error), Toast.LENGTH_SHORT).show();
            }
        });

        dictListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (view != null)
                {
                    TextView textVie = (TextView) view;
                    String dictName = textVie.getText().toString();
                    editorVM.getAllWordsByDictName(dictName);
                    List<String> list = mainVM.getDictionaryList().getValue();
                    if (list != null && !list.isEmpty())
                    {
                        editorVM.setDictsToMove(list);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });

        editorVM.wordIsStudied.observe(this, isStudied -> {
            if (isStudied != null && editorVM.selectedWord != null)
            {
                if (isStudied)
                {
                    editorVM.selectedWord.setCountRepeat(-1);
                }
                else
                {
                    editorVM.selectedWord.setCountRepeat(1);
                }
                editorVM.updateWordInDb(Collections.singletonList(editorVM.selectedWord));
                listViewAdapter.notifyDataSetChanged();
            }
        });


        editorVM.getEnWord().observe(this, s -> {
            editTextEn.setText(s);
        });

        editorVM.getRuWord().observe(this, s -> {
            editTextRu.setText(s);
        });

        editorVM.isWordUpdated.observe(this, isUpdated -> {
            if (isUpdated != null && isUpdated)
            {
                editorVM.getAllWordsByDictName(dictListSpinner.getSelectedItem().toString());
                AppBus.INSTANCE.updateWords(true);
                Toast.makeText(getApplicationContext(), R.string.text_dict_is_updated, Toast.LENGTH_LONG).show();
            }
        });

        addWordVM.getInsertedId().observe(this, id -> {
            if (id > 0)
            {
                Toast.makeText(getApplicationContext(), R.string.text_dict_is_updated, Toast.LENGTH_LONG).show();
            }
        });

        mainVM.getCountRepeat().observe(this, id -> {
            if (id != null && id > 0)
            {
                editorVM.getAllWordsByDictName(dictListSpinner.getSelectedItem().toString());
                AppBus.INSTANCE.updateWords(true);
            }
        });

        String enWord = getIntent().getStringExtra(KEY_EXTRA_EN_WORD);
        String ruWord = getIntent().getStringExtra(KEY_EXTRA_RU_WORD);
        if (enWord != null && ruWord != null)
        {
            editorVM.setEnWord(enWord);
            editorVM.setRuWord(ruWord);
            switcher.showNext();
        }


    }

    @Override
    public void onBackPressed()
    {
        int index = switcher.getDisplayedChild();
        if (index > 0) {
            switcher.showPrevious();
        }
        else {
            setResult(requestCode, new Intent());
            finish();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void buttonCancel_OnClick()
    {
        buttonCancel.setOnClickListener(v -> switcher.showPrevious());
    }

    private void buttonDelete_OnClick()
    {
        buttonDelete.setOnClickListener( v ->
        {
            final String tableName = dictListSpinner.getSelectedItem().toString();

            ExtensionsKt.showDialogAsSingleton(
                    this,
                    ConfirmDialog.Companion.newInstance((dialog, binding) -> {

                        binding.tvEmoji.setVisibility(View.GONE);
                        binding.tvEmoji2.setVisibility(View.GONE);
                        binding.ivIcon.setVisibility(View.VISIBLE);
                        binding.ivIcon.setImageResource(R.drawable.ic_warning);
                        String message = getString(R.string.dialog_msg_delete_word) + tableName + "?";
                        binding.tvMessage.setText(message);
                        binding.btnOk.setText(R.string.button_text_yes);
                        binding.btnOk.setOnClickListener(view -> {
                            if (editorVM.selectedWord != null)
                            {
                                editorVM.deleteWordFromDb(editorVM.selectedWord);
                            }
                            switcher.showPrevious();
                            dialog.dismiss();
                        });
                        binding.btnCancel.setText(R.string.button_text_no);
                        binding.btnCancel.setOnClickListener(view -> {
                            dialog.dismiss();
                        });
                        return null;
                    }), ConfirmDialog.Companion.getTAG());
        });
    }

    private void buttonWrite_OnClick()
    {
        buttonWrite.setOnClickListener(v -> {
            if (editorVM.selectedWord != null)
            {
                int id = editorVM.selectedWord.get_id();
                String dict = editorVM.selectedWord.getDictName();
                String enWord = editTextEn.getText().toString();
                String ruWord = editTextRu.getText().toString();
                int repeat = 1;
                CheckBox checkEnable = findViewById(R.id.checkStudied2);
                if (checkEnable.isChecked())
                {
                    repeat = -1;
                }
                Word word = new Word(id, dict, enWord, ruWord, repeat);
                if (checkMove.isChecked())
                {
                    String otherDict = spinnerDictToMove.getSelectedItem().toString();
                    word.set_id(0);
                    word.setDictName(otherDict);
                    if (checkCopy.isChecked())
                    {
                        addWordVM.insertEntryAsync(word);
                    }
                    else
                    {
                        addWordVM.insertEntryAsync(word);
                        editorVM.deleteWordFromDb(editorVM.selectedWord);
                    }
                }
                else
                {
                    editorVM.updateWordInDb(Collections.singletonList(word));
                }
            }
            switcher.showPrevious();
        });
    }

    @SuppressWarnings("CodeBlock2Expr")
    private void checkMove_OnClick()
    {
        checkMove.setOnClickListener(v ->
        {
            editorVM.setMoveWord(checkMove.isChecked());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.d_word_editor_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.word_search);
        searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        EditorSearchViewModel viewModel = new ViewModelProvider(this).get(EditorSearchViewModel.class);

        if (searchManager != null)
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView_onListeners(searchView);
        if (viewModel.searchIsActive.getValue() != null)
        {
            searchView.setIconified(viewModel.searchIsActive.getValue());
            searchView.setQuery(viewModel.queryString.getValue(), false);
            searchView.setOnSearchClickListener(view -> {
                viewModel.setSearchAsActive(true);
                searchView.setQuery(viewModel.queryString.getValue(), false);
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
            mainVM.setCountRepeat(1, 1, Integer.MAX_VALUE);
        }
        if (id == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
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
                    // Hint: Фильтрация ListView, вызов
                    listViewAdapter = (ListViewAdapter) listView.getAdapter();
                    if (listViewAdapter != null)
                    {
                        listViewAdapter.getFilter().filter(newText);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }


    public void btn_Speak_OnClick(View view)
    {
        String text = editTextEn.getText().toString();
        if (!text.equals(""))
        {
            try
            {
                spechVM.doSpeech(text, Locale.US);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClickListener(Word word)
    {
        editorVM.selectedWord = word;
        editorVM.setEnWord(word.getEnglish());
        editorVM.setRuWord(word.getTranslate());
        CheckBox checkBox = findViewById(R.id.checkStudied2);
        checkBox.setChecked(word.getCountRepeat() <= 0);
        switcher.showNext();
    }

    @Override
    public void onItemCheckBoxClickListener(Word word)
    {
        if (word.getCountRepeat() < 0 )
        {
            editorVM.disableWord(false);
            Toast.makeText(WordEditorActivity.this, getString(R.string.text_word_is_not_show), Toast.LENGTH_LONG).show();
        }
        else
        {
            editorVM.disableWord(true);
            Toast.makeText(WordEditorActivity.this, getString(R.string.text_word_is_enabled), Toast.LENGTH_LONG).show();
        }
        editorVM.updateWordInDb(Collections.singletonList(word));
    }
}
