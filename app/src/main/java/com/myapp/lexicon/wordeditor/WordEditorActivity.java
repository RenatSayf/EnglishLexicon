package com.myapp.lexicon.wordeditor;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.addword.AddWordViewModel;
import com.myapp.lexicon.helpers.AppBus;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.JavaKotlinMediator;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.main.SpeechViewModel;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.viewmodels.EditorSearchViewModel;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequestError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class WordEditorActivity extends AppCompatActivity implements ListViewAdapter.IListViewAdapter
{
    public static final String KEY_EXTRA_DICT_NAME = "wordeditor_dict_name";

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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

    }

    @SuppressWarnings("CodeBlock2Expr")
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
                Word currentWord = mainVM.getCurrentWord().getValue();
                if (currentWord != null)
                {
                    int index = dicts.indexOf(currentWord.getDictName());
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
            listView.setAdapter(listViewAdapter); // TODO: ListView setAdapter
            progressBar.setVisibility(View.GONE);
            String text = getString(R.string.text_words) + "  " + words.size();
            tvAmountWords.setText(text);
        });

        ExtensionsKt.checkAdsToken(this, () -> {

            BannerAdView adBanner = findViewById(R.id.banner_editor);
            if (adBanner != null)
            {
                JavaKotlinMediator mediator = new JavaKotlinMediator();
                mediator.loadBannerAd(this, 1, adBanner, new JavaKotlinMediator.BannerAdListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        if (BuildConfig.DEBUG)
                        {
                            System.out.println("************* Banner is loaded ******************");
                        }
                    }

                    @Override
                    public void onError(@NonNull AdRequestError error)
                    {
                        if (BuildConfig.DEBUG)
                        {
                            System.out.println("**************** Banner Error" + error.getDescription() + " *******************");
                        }
                    }
                });
            }
            return null;
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
                Toast.makeText(getApplicationContext(), "Словарь обновлен...", Toast.LENGTH_LONG).show();
            }
        });

        addWordVM.getInsertedId().observe(this, id -> {
            if (id > 0)
            {
                Toast.makeText(getApplicationContext(), "Словарь обновлен...", Toast.LENGTH_LONG).show();
            }
        });

        mainVM.getCountRepeat().observe(this, id -> {
            if (id != null && id > 0)
            {
                editorVM.getAllWordsByDictName(dictListSpinner.getSelectedItem().toString());
                AppBus.INSTANCE.updateWords(true);
            }
        });

        Word wordFromMainActivity = AppBus.INSTANCE.getWord().getValue(); // получение слова из MainActivity
        if (wordFromMainActivity != null)
        {
            editorVM.setEnWord(wordFromMainActivity.getEnglish());
            editorVM.setRuWord(wordFromMainActivity.getTranslate());
            switcher.showNext();
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
            LockOrientation orientation = new LockOrientation(WordEditorActivity.this);
            orientation.lock();
            final String tableName = dictListSpinner.getSelectedItem().toString();

            new AlertDialog.Builder(WordEditorActivity.this) // TODO: AlertDialog с макетом по умолчанию
                    .setTitle(R.string.dialog_title_confirm_action)
                    .setIcon(R.drawable.icon_warning)
                    .setMessage(getString(R.string.dialog_msg_delete_word) + tableName + "?")
                    .setPositiveButton(R.string.button_text_yes, (dialog, which) -> {
                        orientation.unLock();
                        if (editorVM.selectedWord != null)
                        {
                            editorVM.deleteWordFromDb(editorVM.selectedWord);
                        }
                        switcher.showPrevious();
                    })
                    .setNegativeButton(R.string.button_text_no, (dialog, which) -> orientation.unLock())
                    .create().show();
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
                    //// TODO: Фильтрация ListView, вызов
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
        editorVM.updateWordInDb(Collections.singletonList(word)); //TODO надо проверить обновление слова
    }
}
