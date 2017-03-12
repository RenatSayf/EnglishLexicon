package com.myapp.lexicon;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.main.SplashScreenActivity;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class b_AddWordActivity extends AppCompatActivity
{
    private EditText textViewEnter;
    private LinearLayout layoutLinkYa;
    private TextView textViewLinkYandex;
    private EditText textViewResult;
    private ImageButton buttonEdit;
    private Button buttonTrans;
    private ProgressBar progressBar;
    private ProgressBar progressBarEn;
    private ProgressBar progressBarRu;
    private Spinner spinnerListDict;
    private Button buttonAddWord;
    private String langSystem;
    private ImageButton button_sound1;
    private ImageButton button_sound2;
    private ImageButton button_swap;
    private ImageButton buttonClean1, buttonClean2;
    private DataBaseQueries dataBaseQueries;
    private TextToSpeech speechText;
    private boolean speech_able_en = true;
    private boolean speech_able_ru = true;
    private HashMap<String, String> utterance_Id = new HashMap<>();
    private boolean flag_btn_trans_click = false;

    private Intent tests_activity;
    private Intent play_list_activity;
    private Intent word_editor_activity;

    private static int transCounter = 0;

    private void initViews() throws SQLException
    {
        textViewEnter = (EditText) findViewById(R.id.textViewEnter);
        textViewEnter_onChange();
        layoutLinkYa = (LinearLayout) findViewById(R.id.lin_layout_link_ya);
        layoutLinkYa.setVisibility(View.GONE);
        textViewLinkYandex = (TextView) findViewById(R.id.textViewLinkYandex);
        textViewLinkYandex.setText(Html.fromHtml(getResources().getString(R.string.link_to_yandex_trans)));
        textViewResult = (EditText) findViewById(R.id.textViewResult);
        textViewResult.setRawInputType(0x00000000);
        textViewResult_onChange();
        buttonEdit = (ImageButton) findViewById(R.id.buttonEdit);
        buttonTrans = (Button) findViewById(R.id.button_trans);
        buttonTrans_onClick();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        progressBarEn = (ProgressBar) findViewById(R.id.progressEn);
        progressBarEn.setVisibility(View.GONE);
        progressBarRu = (ProgressBar) findViewById(R.id.progressRu);
        progressBarRu.setVisibility(View.GONE);
        spinnerListDict = (Spinner) findViewById(R.id.spinn_dict_to);
        spinnerListDict_onItemSelected();
        buttonAddWord = (Button) findViewById(R.id.button_add);
        buttonAddWord_onClick();
        langSystem = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        button_sound1 = (ImageButton) findViewById(R.id.btn_speech);
        button_sound2 = (ImageButton) findViewById(R.id.btn_sound2);
        button_sound1_onClick();
        button_sound2_onClick();
        button_swap = (ImageButton) findViewById(R.id.btn_swap);
        buttonSwap_onClick();
        buttonClean1 = (ImageButton) findViewById(R.id.btn_clean1);
        buttonClean2 = (ImageButton) findViewById(R.id.btn_clean2);
        buttonClean_onClick();
    }

    private void initTTS()
    {
        speechText = new TextToSpeech(b_AddWordActivity.this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "text_id");
                if (status == TextToSpeech.SUCCESS)
                {
                    int resultRu = speechText.isLanguageAvailable(Locale.getDefault());
                    if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        speech_able_ru = false;
                    }
                    int resultEn = speechText.isLanguageAvailable(Locale.UK);
                    if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        speech_able_en = false;
                    }
                }else
                {
                    speech_able_en = false;
                    speech_able_ru = false;
                }
                z_Log.v("status = "+status+"   speech_able_en = "+speech_able_en+"      speech_able_ru = "+speech_able_ru);

                speechText.setOnUtteranceProgressListener(new UtteranceProgressListener()
                {
                    @Override
                    public void onStart(String utteranceId)
                    {
                        progressBarEn.setVisibility(View.GONE);
                        progressBarRu.setVisibility(View.GONE);
                    }

                    @Override
                    public void onDone(String utteranceId)
                    {
                        progressBarEn.setVisibility(View.GONE);
                        progressBarRu.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String utteranceId)
                    {
                        z_Log.v("Ошибка синтеза");
                    }
                });
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_layout_add_word);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try
        {
            initViews();
            dataBaseQueries = new DataBaseQueries(this);
            dataBaseQueries.setListTableToSpinner(spinnerListDict, 0);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        initTTS();

        if (!isOnline(this) && savedInstanceState == null)
        {
            Toast toast = Toast.makeText(this, R.string.msg_not_internet, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        if (savedInstanceState != null && transCounter > 0)
        {
            if (layoutLinkYa.getVisibility() == View.GONE)
            {
                layoutLinkYa.setVisibility(View.VISIBLE);
            }
        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.b_add_word_menu, menu);
//        tests_activity = new Intent();
//        tests_activity.setClass(this, t_Tests.class);
//
//        play_list_activity = new Intent();
//        play_list_activity.setClass(this, p_PlayList.class);
//
//        word_editor_activity = new Intent();
//        word_editor_activity.setClass(this, d_WordEditor.class);
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
            case R.id.menu_tests:
                startActivity(tests_activity);
                break;
            case R.id.menu_play_list:
                startActivity(play_list_activity);
                break;
            case R.id.menu_word_editor:
                startActivity(word_editor_activity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void textViewLinkYandex_onClick(View view)
    {
        textViewLinkYandex.setTextColor(Color.RED);
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.translate_yandex_ru)));
        startActivity(browser);
    }
    private boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }
    public void buttonEdit_onClick(View view)
    {
        textViewResult.setRawInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        if (!textViewResult.isFocused())
        {
            textViewResult.requestFocus();
        }
    }
    public void textViewEnter_onChange()
    {
        textViewEnter.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (!textViewEnter.getText().toString().equals("") && isOnline(b_AddWordActivity.this))
                {
                    buttonTrans.setEnabled(true);

                }else
                {
                    buttonTrans.setEnabled(false);

                }

                if (textViewResult.getText().toString().equals("") || textViewEnter.getText().toString().equals(""))
                {
                    buttonAddWord.setEnabled(false);
                    //textViewLinkYandex.setVisibility(View.GONE);
                }else
                {
                    buttonAddWord.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }
    private void buttonTrans_onClick()
    {
        buttonTrans.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                flag_btn_trans_click = true;
                b_OnlineTranslatorApi translatorApi = new b_OnlineTranslatorApi(textViewResult, progressBar);
                translatorApi.getTranslateAsync(textViewEnter.getText().toString());
                transCounter++;
            }
        });
    }
    private void textViewResult_onChange()
    {
        textViewResult.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (isOnline(b_AddWordActivity.this) && flag_btn_trans_click)
                {
                    //textViewLinkYandex.setVisibility(View.VISIBLE);
                    layoutLinkYa.setVisibility(View.VISIBLE);
                    flag_btn_trans_click = false;
                }

                if (!textViewResult.getText().toString().equals("") && !textViewEnter.getText().toString().equals(""))
                {
                    buttonAddWord.setEnabled(true);
                }else
                {
                    buttonAddWord.setEnabled(false);
                    //textViewLinkYandex.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }
    private int checkText(String text1, String text2)
    {
        Pattern patternEn = Pattern.compile(
                        "[" +                   //начало списка допустимых символов
                        "a-zA-Z" +              //буквы английского алфавита
                        "\\d" +                 //цифры
                        "\\s" +                 //знаки-разделители (пробел, табуляция и т.д.)
                        "\\p{Punct}" +          //знаки пунктуации
                        "]" +                   //конец списка допустимых символов
                        "*");                   //допускается наличие указанных символов в любом количестве
        Matcher matcherEn = patternEn.matcher(text1);

        Pattern patternRu = Pattern.compile(
                        "[" +                   //начало списка допустимых символов
                        "а-яА-ЯёЁ" +            //буквы русского алфавита
                        "\\d" +                 //цифры
                        "\\s" +                 //знаки-разделители (пробел, табуляция и т.д.)
                        "\\p{Punct}" +          //знаки пунктуации
                        "]" +                   //конец списка допустимых символов
                        "*");                   //допускается наличие указанных символов в любом количестве
        Matcher matcherRu = patternRu.matcher(text2);
        if (matcherEn.matches() && matcherRu.matches())
        {
            return 1;
        }
        if (!matcherEn.matches() && !matcherRu.matches())
        {
            return -1;
        }
        return 0;
    }
    private void buttonAddWord_onClick() throws SQLException
    {
        buttonAddWord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                long id = -1;
                DataBaseEntry entry = new DataBaseEntry(null, null, null);
                int res = checkText(textViewEnter.getText().toString(), textViewResult.getText().toString());
                if (res == 1)
                {
                    entry.set_english(textViewEnter.getText().toString());
                    entry.set_translate(textViewResult.getText().toString());
                    id = dataBaseQueries.insertWordInTable(selectDict, entry);
                }
                if (res == -1)
                {
                    entry.set_english(textViewResult.getText().toString());
                    entry.set_translate(textViewEnter.getText().toString());
                    id = dataBaseQueries.insertWordInTable(selectDict, entry);
                }
                if (id != -1)
                {
                    Toast toast = Toast.makeText(b_AddWordActivity.this, getString(R.string.in_dictionary)+selectDict+getString(R.string.new_word_is_added), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    textViewEnter.setText("");
                    textViewResult.setText("");
                }
            }
        });
    }
    private String selectDict;
    private void spinnerListDict_onItemSelected()
    {
        spinnerListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectDict = spinnerListDict.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }
    private String getLangOfText(String text)
    {
        String lang = null;
        Pattern patternEn = Pattern.compile(
                        "[" +                   //начало списка допустимых символов
                        "a-zA-Z" +              //буквы английского алфавита
                        "\\d" +                 //цифры
                        "\\s" +                 //знаки-разделители (пробел, табуляция и т.д.)
                        "\\p{Punct}" +          //знаки пунктуации
                        "]" +                   //конец списка допустимых символов
                        "*");                   //допускается наличие указанных символов в любом количестве
        Matcher matcherEn = patternEn.matcher(text);

        Pattern patternRu = Pattern.compile(
                        "[" +                   //начало списка допустимых символов
                        "а-яА-ЯёЁ" +            //буквы русского алфавита
                        "\\d" +                 //цифры
                        "\\s" +                 //знаки-разделители (пробел, табуляция и т.д.)
                        "\\p{Punct}" +          //знаки пунктуации
                        "]" +                   //конец списка допустимых символов
                        "*");                   //допускается наличие указанных символов в любом количестве
        Matcher matcherRu = patternRu.matcher(text);
        if (matcherEn.matches())
        {
            lang = "en";
        }
        else
        if (matcherRu.matches())
        {
            lang = "ru";
        }
        return lang;
    }
    private void button_sound1_onClick()
    {
        button_sound1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String text1 = textViewEnter.getText().toString();
                if (speech_able_en && !text1.equals("") && getLangOfText(text1).equals("en"))
                {
                    progressBarEn.setVisibility(View.VISIBLE);
                    utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "En");
                    speechText.setLanguage(Locale.US);
                    speechText.speak(text1, TextToSpeech.QUEUE_ADD, utterance_Id);
                }
                if (speech_able_ru && !text1.equals("") && getLangOfText(text1).equals("ru"))
                {
                    progressBarEn.setVisibility(View.VISIBLE);
                    utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Ru");
                    speechText.setLanguage(Locale.getDefault());
                    speechText.speak(text1, TextToSpeech.QUEUE_ADD, utterance_Id);
                }
            }
        });
    }

    private void button_sound2_onClick()
    {
        button_sound2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String text2 = textViewResult.getText().toString();
                if (speech_able_en && !text2.equals("") && getLangOfText(text2).equals("en"))
                {
                    progressBarRu.setVisibility(View.VISIBLE);
                    SplashScreenActivity.speech.setLanguage(Locale.US);
                    SplashScreenActivity.speech.speak(text2, TextToSpeech.QUEUE_ADD, SplashScreenActivity.map);
                }
                if (speech_able_ru && !text2.equals("") && getLangOfText(text2).equals("ru"))
                {
                    progressBarRu.setVisibility(View.VISIBLE);
                    SplashScreenActivity.speech.setLanguage(Locale.getDefault());
                    SplashScreenActivity.speech.speak(text2, TextToSpeech.QUEUE_ADD, SplashScreenActivity.map);
                }
            }
        });
    }

    private void buttonSwap_onClick()
    {
        button_swap.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String temp = textViewEnter.getText().toString();
                textViewEnter.setText(textViewResult.getText());
                textViewResult.setText(temp);
            }
        });
    }

    private void buttonClean_onClick()
    {
        buttonClean1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                textViewEnter.setText(null);
            }
        });

        buttonClean2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                textViewResult.setText(null);
            }
        });
    }



}
