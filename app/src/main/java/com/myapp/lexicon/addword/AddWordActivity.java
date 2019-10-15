package com.myapp.lexicon.addword;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.GetTableListLoader2;
import com.myapp.lexicon.dialogs.NewDictDialog;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

public class AddWordActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks, NewDictDialog.INewDictDialogResult
{
    public static final int GOOGLE_SIGN_IN_CODE = 10;

    private AutoCompleteTextView textViewEnter;
    private ConstraintLayout layoutLinkYa;
    private TextView textViewLinkYandex;
    private EditText textViewResult;
    private Button buttonTrans, btnNewDict;
    private ProgressBar progressBar;
    private Spinner spinnerListDict;
    private ArrayList<String> spinnerItems = new ArrayList<>();
    private Button buttonAddWord;
    private ImageButton button_sound1;
    private ImageButton button_sound2;
    private ImageButton button_swap;
    private ImageButton buttonClean1, buttonClean2;
    private HashMap<String, String> utterance_Id = new HashMap<>();
    private boolean flag_btn_trans_click = false;
    private ErrorHandlerDialog errorHandlerDialog;
    private AppSettings appSettings;

    private final int LOADER_GET_TABLE_LIST = 11;
    private final int LOADER_GET_TRANSLATE = 12;

    private final String KEY_SELECT_SPINNER_INDEX = "key_spinner";
    private final String KEY_SPINNER_ITEMS = "key_spinner_items";
    private final String KEY_TEXT_RESULT_ENABLED = "key_text_result_enabled";

    private static int transCounter = 0;

    private void initViews()
    {
        textViewEnter = findViewById(R.id.textViewEnter);
        textViewEnter_onChange();
        layoutLinkYa = findViewById(R.id.lin_layout_link_ya);
        if (layoutLinkYa != null)
        {
            layoutLinkYa.setVisibility(View.GONE);
        }
        textViewLinkYandex = findViewById(R.id.textViewLinkYandex);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
        {
            textViewLinkYandex.setText(Html.fromHtml(getResources().getString(R.string.link_to_yandex_trans), Html.FROM_HTML_MODE_COMPACT));
        } else
        {
            textViewLinkYandex.setText(Html.fromHtml(getResources().getString(R.string.link_to_yandex_trans)));
        }
        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setEnabled(false);
        if (textViewResult != null)
        {
            textViewResult.setRawInputType(TYPE_TEXT_FLAG_MULTI_LINE);
        }
        textViewResult_onChange();
        buttonTrans = findViewById(R.id.button_trans);
        buttonTrans_onClick();
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null)
        {
            progressBar.setVisibility(View.GONE);
        }
        spinnerListDict = findViewById(R.id.spinn_dict_to);
        spinnerListDict_onItemSelected();
        buttonAddWord = findViewById(R.id.button_add);
        buttonAddWord_onClick();
        button_sound1 = findViewById(R.id.btn_speech);
        button_sound2 = findViewById(R.id.btn_sound2);
        button_sound1_onClick();
        button_sound2_onClick();
        button_swap = findViewById(R.id.btn_swap);
        buttonSwap_onClick();
        buttonClean1 = findViewById(R.id.btn_clean1);
        buttonClean2 = findViewById(R.id.btn_clean2);
        buttonClean_onClick();
        btnNewDict = findViewById(R.id.btn_new_dict);
        btnNewDict_onClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_layout_add_word);
        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

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

        if (savedInstanceState != null)
        {
            ArrayList<String> list = savedInstanceState.getStringArrayList(KEY_SPINNER_ITEMS);
            if (list != null  && list.size() > 0)
            {
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
                spinnerListDict.setAdapter(adapterSpinner);
                spinnerListDict.setSelection(savedInstanceState.getInt(KEY_SELECT_SPINNER_INDEX));
            }
            textViewResult.setEnabled(savedInstanceState.getBoolean(KEY_TEXT_RESULT_ENABLED));
        }
        else
        {
            getLoaderManager().restartLoader(LOADER_GET_TABLE_LIST, null, AddWordActivity.this).forceLoad();
        }
        getLoaderManager().initLoader(LOADER_GET_TABLE_LIST, savedInstanceState, this);
        getLoaderManager().initLoader(LOADER_GET_TRANSLATE, savedInstanceState, this);

        if (AppData.getInstance().isAdMob())
        {
            if (AppData.getInstance().isOnline(this))
            {
                if (savedInstanceState == null)
                {
                    BannerFragmentAW bannerFragment = new BannerFragmentAW();
                    getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_aw, bannerFragment).commit();
                }
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        appSettings = new AppSettings(this);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_SPINNER_ITEMS, spinnerItems);
        outState.putInt(KEY_SELECT_SPINNER_INDEX, spinnerListDict.getSelectedItemPosition());
        outState.putBoolean(KEY_TEXT_RESULT_ENABLED, textViewResult.isEnabled());
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.b_add_word_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.enter_key)
        {
            errorHandlerDialog = new ErrorHandlerDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(ErrorHandlerDialog.KEY_IS_SAD_FACE, false);
            String error = getString(R.string.dialog_error_text_0);
            bundle.putString(ErrorHandlerDialog.KEY_ERROR_MESSAGE, error);
            String option = getString(R.string.dialog_option_text_0);
            bundle.putString(ErrorHandlerDialog.KEY_OPTION_MESSAGE, option);
            errorHandlerDialog.setArguments(bundle);
            errorHandlerDialog.setCancelable(false);
            errorHandlerDialog.show(getSupportFragmentManager(), ErrorHandlerDialog.DIALOG_TAG);
        }
        if (item.getItemId() == R.id.save_db_to_google_drive)
        {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    public void textViewLinkYandex_onClick(View view)
    {
        textViewLinkYandex.setTextColor(Color.RED);
        String text = textViewEnter.getText().toString();
        String transDirect;
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
        {
            transDirect = getString(R.string.translate_direct_en_ru);
        }
        else
        {
            transDirect = getString(R.string.translate_direct_ru_en);
        }
        String url = getString(R.string.translate_yandex_ru).concat("/?lang=").concat(transDirect).concat("&text=").concat(text);
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browser);
    }

    private boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null)
        {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } else
        {
            return false;
        }
    }

    public void buttonEdit_onClick(View view)
    {
        textViewResult.setEnabled(true);
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
                if (!textViewEnter.getText().toString().equals("") && isOnline(AddWordActivity.this))
                {
                    buttonTrans.setEnabled(true);

                }else
                {
                    buttonTrans.setEnabled(false);
                }

                if (textViewResult.getText().toString().equals("") || textViewEnter.getText().toString().equals(""))
                {
                    buttonAddWord.setEnabled(false);
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
                progressBar.setVisibility(View.VISIBLE);
                Bundle bundle = new Bundle();
                bundle.putString(GetTranslateLoader.KEY_TEXT_ENTERED, textViewEnter.getText().toString());
                getLoaderManager().restartLoader(LOADER_GET_TRANSLATE, bundle, AddWordActivity.this).forceLoad();
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
                if (isOnline(AddWordActivity.this) && flag_btn_trans_click)
                {
                    layoutLinkYa.setVisibility(View.VISIBLE);
                    flag_btn_trans_click = false;
                }

                if (!textViewResult.getText().toString().equals("") && !textViewEnter.getText().toString().equals(""))
                {
                    buttonAddWord.setEnabled(true);
                }else
                {
                    buttonAddWord.setEnabled(false);
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

    private void buttonAddWord_onClick()
    {
        buttonAddWord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                long id = -1;
                DataBaseQueries dataBaseQueries = new DataBaseQueries(AddWordActivity.this);
                DataBaseEntry entry = new DataBaseEntry(null, null, null);
                int res = checkText(textViewEnter.getText().toString(), textViewResult.getText().toString());
                if (res == 1)
                {
                    entry.setEnglish(textViewEnter.getText().toString());
                    entry.setTranslate(textViewResult.getText().toString());
                    id = dataBaseQueries.insertWordInTableSync(selectDict, entry);
                }
                if (res == -1)
                {
                    entry.setEnglish(textViewResult.getText().toString());
                    entry.setTranslate(textViewEnter.getText().toString());
                    id = dataBaseQueries.insertWordInTableSync(selectDict, entry);
                }
                if (id != -1)
                {
                    Toast toast = Toast.makeText(AddWordActivity.this, getString(R.string.in_dictionary)+selectDict+getString(R.string.new_word_is_added), Toast.LENGTH_SHORT);
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
        String lang = "";
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
                        "А-Яа-яЁёЇїІіЄєҐґ" +    //буквы русского и украинского алфавита
                        "\\d" +                 //цифры
                        "\\s" +                 //знаки-разделители (пробел, табуляция и т.д.)
                        "\\p{Punct}" +          //знаки пунктуации
                        "]" +                   //конец списка допустимых символов
                        "*");                   //допускается наличие указанных символов в любом количестве
        Matcher matcherRu = patternRu.matcher(text);
        if (matcherEn.matches())
        {
            lang = getString(R.string.translate_direct_en_ru);
        }
        else
        if (matcherRu.matches())
        {
            lang = getString(R.string.translate_direct_ru_en);
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
                if (SplashScreenActivity.speech != null)
                {
                    String text1 = textViewEnter.getText().toString();
                    if (!text1.equals("") && getLangOfText(text1).equals(AddWordActivity.this.getString(R.string.translate_direct_en_ru)))
                    {
                        try
                        {
                            SplashScreenActivity.speech.setLanguage(Locale.US);
                        } catch (Exception e)
                        {
                            return;
                        }
                        utterance_Id.clear();
                        utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "add_word_us");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            SplashScreenActivity.speech.speak(text1, TextToSpeech.QUEUE_ADD, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        } else
                        {
                            SplashScreenActivity.speech.speak(text1, TextToSpeech.QUEUE_ADD, utterance_Id);
                        }
                    }
                    if (!text1.equals("") && getLangOfText(text1).equals(AddWordActivity.this.getString(R.string.translate_direct_ru_en)))
                    {
                        try
                        {
                            SplashScreenActivity.speech.setLanguage(new Locale(appSettings.getTransLang()));
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                            SplashScreenActivity.speech.setLanguage(Locale.getDefault());
                        }
                        utterance_Id.clear();
                        utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "add_word_ru");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            SplashScreenActivity.speech.speak(text1, TextToSpeech.QUEUE_ADD, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        } else
                        {
                            SplashScreenActivity.speech.speak(text1, TextToSpeech.QUEUE_ADD, utterance_Id);
                        }
                    }
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
                if (!text2.equals("") && getLangOfText(text2).equals(getString(R.string.translate_direct_en_ru)) && SplashScreenActivity.speech != null)
                {
                    try
                    {
                        SplashScreenActivity.speech.setLanguage(Locale.US);
                    } catch (Exception e)
                    {
                        return;
                    }
                    utterance_Id.clear();
                    utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "add_word_us");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        SplashScreenActivity.speech.speak(text2, TextToSpeech.QUEUE_ADD, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                    } else
                    {
                        SplashScreenActivity.speech.speak(text2, TextToSpeech.QUEUE_ADD, utterance_Id);
                    }
                }
                if (!text2.equals("") && getLangOfText(text2).equals(getString(R.string.translate_direct_ru_en)) && SplashScreenActivity.speech != null)
                {
                    try
                    {
                        SplashScreenActivity.speech.setLanguage(new Locale(appSettings.getTransLang()));
                    } catch (Exception e)
                    {
                        SplashScreenActivity.speech.setLanguage(Locale.getDefault());
                    }
                    utterance_Id.clear();
                    utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "add_word_ru");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        SplashScreenActivity.speech.speak(text2, TextToSpeech.QUEUE_ADD, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                    } else
                    {
                        SplashScreenActivity.speech.speak(text2, TextToSpeech.QUEUE_ADD, utterance_Id);
                    }
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


    @Override
    public Loader onCreateLoader(int id, Bundle bundle)
    {
        Loader loader = null;
        switch (id)
        {
            case LOADER_GET_TABLE_LIST:
                loader = new GetTableListLoader2(this);
                break;
            case LOADER_GET_TRANSLATE:
                loader = new GetTranslateLoader(this, bundle);
                break;
            default:
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data)
    {
        if (loader.getId() == LOADER_GET_TABLE_LIST)
        {
            //noinspection unchecked
            spinnerItems = (ArrayList<String>) data;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerItems);
            spinnerListDict.setAdapter(adapter);
        }
        if (loader.getId() == LOADER_GET_TRANSLATE)
        {
            @SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) data;
            if (list.size() > 1)
            {
                textViewResult.setText(list.get(1));
                httpErrorHandler(list.get(0));
            }
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader loader)
    {
        progressBar.setVisibility(View.GONE);
    }

    private void httpErrorHandler(String error)
    {
        String message;
        if (error != null && error.equals("200"))
        {
            return;
        }

        errorHandlerDialog = new ErrorHandlerDialog();
        errorHandlerDialog.setCancelable(false);
        Bundle bundle = new Bundle();

        if (error != null && error.equals("401"))
        {
            message = getString(R.string.http_error_401);
            bundle.putBoolean(ErrorHandlerDialog.KEY_IS_SAD_FACE, true);
            bundle.putString(ErrorHandlerDialog.KEY_ERROR_MESSAGE, message);
            bundle.putString(ErrorHandlerDialog.KEY_OPTION_MESSAGE, getString(R.string.dialog_option_text_0));
            errorHandlerDialog.setArguments(bundle);
            errorHandlerDialog.show(getSupportFragmentManager(), ErrorHandlerDialog.DIALOG_TAG);
        }
        if (error != null && error.equals("402"))
        {
            message = getString(R.string.http_error_402);
            bundle.putBoolean(ErrorHandlerDialog.KEY_IS_SAD_FACE, true);
            bundle.putString(ErrorHandlerDialog.KEY_ERROR_MESSAGE, message);
            bundle.putString(ErrorHandlerDialog.KEY_OPTION_MESSAGE, getString(R.string.dialog_option_text_0));
            errorHandlerDialog.setArguments(bundle);
            errorHandlerDialog.show(getSupportFragmentManager(), ErrorHandlerDialog.DIALOG_TAG);
        }
        if (error != null && error.equals("404"))
        {
            message = getString(R.string.http_error_404);
            bundle.putBoolean(ErrorHandlerDialog.KEY_IS_SAD_FACE, true);
            bundle.putString(ErrorHandlerDialog.KEY_ERROR_MESSAGE, message);
            bundle.putString(ErrorHandlerDialog.KEY_OPTION_MESSAGE, getString(R.string.dialog_option_text_0));
            errorHandlerDialog.setArguments(bundle);
            errorHandlerDialog.show(getSupportFragmentManager(), ErrorHandlerDialog.DIALOG_TAG);
        }
        if (error != null && error.equals("413"))
        {
            Toast.makeText(this, R.string.http_error_413, Toast.LENGTH_LONG).show();
        }
        if (error != null && error.equals("422"))
        {
            Toast.makeText(this, R.string.http_error_422, Toast.LENGTH_LONG).show();
        }
        if (error != null && error.equals("501"))
        {
            Toast.makeText(this, R.string.http_error_501, Toast.LENGTH_LONG).show();
        }
    }


    public void btnMicrophone_OnClick(View view)
    {
        if (!isOnline(this))
        {
            Toast toast = Toast.makeText(this, R.string.text_voice_input_not_available, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        Intent recognizerIntent = new Intent(Intent.ACTION_VIEW);
        recognizerIntent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.text_say_something));
        try
        {
            startActivityForResult(recognizerIntent, 1);
        } catch (ActivityNotFoundException a)
        {
            Toast.makeText(this, R.string.text_speech_recogniz_not_support, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK && data != null)
            {
                final ArrayList<String> arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (arrayList != null && arrayList.size() > 0)
                {
                    final ArrayAdapter<String> adapter = new ArrayAdapter<>(AddWordActivity.this, android.R.layout.simple_dropdown_item_1line, arrayList);
                    final String oldText = textViewEnter.getText().toString();
                    textViewEnter.setAdapter(adapter);
                    textViewEnter.showDropDown();
                    textViewEnter.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                        {
                            String text = oldText + " " + adapterView.getAdapter().getItem(i).toString();
                            textViewEnter.setText(text);
                        }
                    });
                }
            }
        }
        if (requestCode == GOOGLE_SIGN_IN_CODE)
        {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    public static void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try
        {
            completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e)
        {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w("XXXXXXX", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

    private void btnNewDict_onClick()
    {
        btnNewDict.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentByTag = getFragmentManager().findFragmentByTag(NewDictDialog.TAG);
                if (fragmentByTag != null)
                {
                    fragmentTransaction.remove(fragmentByTag);
                }
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                NewDictDialog newDictDialog = NewDictDialog.newInstance();
                newDictDialog.setNewDictDialogListener(AddWordActivity.this);
                newDictDialog.show(getSupportFragmentManager(), NewDictDialog.TAG);

            }
        });
    }

    @Override
    public void newDictDialogResult(boolean res, String dictName)
    {
        try
        {
            if (res && !dictName.equals(""))
            {
                spinnerItems.add(0, dictName);
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerItems);
                spinnerListDict.setAdapter(adapterSpinner);
                spinnerListDict.setSelection(0);

                Toast toast = Toast.makeText(this, getString(R.string.text_added_new_dict)+dictName, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            else
            {
                Toast toast = Toast.makeText(this, getString(R.string.text_create_dict_fails), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast toast = Toast.makeText(this, getString(R.string.text_create_dict_fails) + "\n" + e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onTranslateDialogEvent(TranslateDialogEvent event)
    {
        DataBaseEntry entry = event.entry;
        String english = entry.getEnglish();
        textViewEnter.setText(english);
        String translate = entry.getTranslate();
        textViewResult.setText(translate);
    }


}
