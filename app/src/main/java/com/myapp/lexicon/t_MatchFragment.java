package com.myapp.lexicon;

import android.animation.Animator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.app.Fragment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link t_MatchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link t_MatchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class t_MatchFragment extends Fragment implements t_DialogTestComplete.IDialogComplete_Result
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    SimpleCursorAdapter scAdapter;



    public t_MatchFragment()
    {
        // Required empty public constructor
        this.setRetainInstance(true);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment t_MatchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static t_MatchFragment newInstance(String param1, String param2)
    {
        t_MatchFragment fragment = new t_MatchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    public static final int ROWS = 5;
    private LinearLayout linLayoutLeft, linLayoutRight;
    private ViewPropertyAnimator animToLeft, animToRight, animToTop, animToDown;
    private long duration = 1000;
    private ArrayList<DataBaseEntry> wordsList = new ArrayList<>();
    private Spinner spinnListDict;
    private ArrayList<String> storedListDict = new ArrayList<>();
    private int spinnSelectedIndex = -1;
    private int btn_left_position;
    private int btn_right_position;
    private int height;
    private int width;
    private int delta = 30;
    private int buttonId;
    private DisplayMetrics metrics;
    private int windowHeight;
    private float textSize;
    private final String LEFT_SIDE = "left";
    private final String RIGHT_SIDE = "right";
    private final String KEY_STORED_LIST_DICT = "storedListDict";
    private DataBaseQueries baseQueries;
    private int wordsCount;
    private int wordsResidue;
    private DataBaseQueries.GetWordsCountAsync getWordsCount;
    private static z_RandomNumberGenerator generatorLeft;
    private static z_RandomNumberGenerator generatorRight;
    private z_LockOrientation lockOrientation;
    private int wordIndex = 1;
    private String KEY_WORD_INDEX = "wordIndex";
    private int counterRightAnswer = 0;
    private ArrayList<String> arrStudiedDict = new ArrayList<>();
    private t_TestResults testResults;
    private t_DialogTestComplete dialogTestComplete;
    private static int[] btnVisibleLeft = new int[ROWS];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View fragment_view = inflater.inflate(R.layout.t_fragment_match, container, false);

        initViews(fragment_view);

        if (savedInstanceState == null)
        {
            setItemsToSpinnListDict();
            for (int i = 0; i < ROWS; i++)
            {
                AppData.arrayBtnLeft[i] = (Button) linLayoutLeft.getChildAt(i);
                AppData.arrayBtnLeft[i].setId(10+i);
                AppData.arrayBtnRight[i] = (Button) linLayoutRight.getChildAt(i);
                AppData.arrayBtnRight[i].setId(20+i);
            }
        }

        if (savedInstanceState != null)
        {
            storedListDict = savedInstanceState.getStringArrayList(KEY_STORED_LIST_DICT);
            ArrayAdapter<String> spinnAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, storedListDict);
            spinnListDict.setAdapter(spinnAdapter);
            wordIndex = savedInstanceState.getInt(KEY_WORD_INDEX);

            for (int i = 0; i < ROWS; i++)
            {
                Button buttonLeft = (Button) linLayoutLeft.getChildAt(i);
                buttonLeft.setText(AppData.arrayBtnLeft[i].getText());
                buttonLeft.setVisibility(AppData.arrayBtnLeft[i].getVisibility());
                buttonLeft.setTextSize(textSize);
                AppData.arrayBtnLeft[i] = buttonLeft;
                btnLeft_OnClick(AppData.arrayBtnLeft[i], i);

                Button buttonRight = (Button) linLayoutRight.getChildAt(i);
                buttonRight.setText(AppData.arrayBtnRight[i].getText());
                buttonRight.setVisibility(AppData.arrayBtnRight[i].getVisibility());
                buttonRight.setTextSize(textSize);
                AppData.arrayBtnRight[i] = buttonRight;
                btnRight_OnClick(AppData.arrayBtnRight[i], i);
            }
        }

        return fragment_view;
    }

    private String KEY_SPINN_LIST_DICT = "spinner";
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        spinnSelectedIndex = spinnListDict.getSelectedItemPosition();
        outState.putInt(KEY_SPINN_LIST_DICT, spinnSelectedIndex);
        outState.putInt(KEY_WORD_INDEX, wordIndex);
        outState.putStringArrayList(KEY_STORED_LIST_DICT, storedListDict);
        for (int i = 0; i < ROWS; i++)
        {
            AppData.arrayBtnRight[i] = (Button) linLayoutRight.getChildAt(i);
            AppData.arrayBtnLeft[i] = (Button) linLayoutLeft.getChildAt(i);
        }

        super.onSaveInstanceState(outState);
    }


    private void initViews(View fragment_view)
    {
        //dialogTestComplete = new t_DialogTestComplete();

        testResults = new t_TestResults(getActivity());
        textSize = getHeightScreen() / 60;
        //region Инициализация экземпляра z_LockOrientation для блокировки смены ориентации экрана
        lockOrientation = new z_LockOrientation(getActivity());
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        //endregion

        linLayoutLeft = (LinearLayout) fragment_view.findViewById(R.id.layout_one_of_five);
        buttonsLeftGone();
        //region получение правого LinearLayout и скрытие его содержимого
        linLayoutRight = (LinearLayout) fragment_view.findViewById(R.id.right_layout);
        buttonsRightGone();
        //endregion
        //region получение Spinner выбора словаря
        spinnListDict = (Spinner) fragment_view.findViewById(R.id.spinn_one_of_five);
        spinnListDict_OnItemSelectedListener();
        //endregion
    }

    private void setItemsToSpinnListDict()
    {
        new DataBaseQueries.GetLictTableAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<String> list)
            {
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, list);
                spinnListDict.setAdapter(adapterSpinner);
                spinnListDict.setSelection(spinnSelectedIndex);
                spinnListDict_OnItemSelectedListener();
                for (int i = 0; i < spinnListDict.getAdapter().getCount(); i++)
                {
                    storedListDict.add(spinnListDict.getAdapter().getItem(i).toString());
                }
            }
        }.execute();
    }

    private void buttonsRightGone()
    {
        for (int i = 0; i < linLayoutRight.getChildCount(); i++)
        {
            Button button = (Button) linLayoutRight.getChildAt(i);
            button.setId(20+i);
            button.setVisibility(View.GONE);
        }
    }

    private void buttonsLeftGone()
    {
        for (int i = 0; i < linLayoutLeft.getChildCount(); i++)
        {
            Button button = (Button) linLayoutLeft.getChildAt(i);
            button.setId(10+i);
            button.setVisibility(View.GONE);
        }
    }

    private String spinnSelectedItem;

    private void spinnListDict_OnItemSelectedListener()
    {
        spinnListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id)
            {
                if (position == spinnSelectedIndex) return;
                startTest(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void startTest(final int position)
    {
        lockOrientation.lock();
        wordIndex = 1;
        guessedWordsCount = 0;
        counterRightAnswer = 0;
        spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        getWordsCount = new DataBaseQueries.GetWordsCountAsync()
        {
            @Override
            public void resultAsyncTask(int res)
            {
                wordsCount = res;
                wordsResidue = wordsCount - ROWS;
                if (wordsResidue > 0)
                {
                    generatorLeft = new z_RandomNumberGenerator(wordsResidue,0);
                    generatorRight = new z_RandomNumberGenerator(wordsResidue, 127);
                }
                getWordsCount = null;
                fillLayoutRight(wordsCount);
                fillLayoutLeft(wordsCount);
                spinnSelectedIndex = position;
                //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        getWordsCount.execute(spinnSelectedItem);
    }

    private void fillLayoutRight(final int rowsCount)
    {
        buttonsRightGone();
        if (rowsCount <= 0) return;
        //lockOrientation.lock();
        int count = rowsCount;
        if (count > ROWS)
        {
            count = ROWS;
        }
        final z_RandomNumberGenerator generator = new z_RandomNumberGenerator(count, 127);
        final int finalCount = count;
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    int randIndex = generator.generate();
                    AppData.arrayBtnRight[i].setText(list.get(randIndex).get_translate());
                    AppData.arrayBtnRight[i].setTextSize(textSize);
                    wordIndex = finalCount;
                }
                //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;

        for (int i = 0; i < count; i++)
        {
            Button button = (Button) linLayoutRight.getChildAt(i);
            button.setVisibility(View.VISIBLE);
            button.setTranslationX(0);
            btnRight_OnClick(button, i);
            AppData.arrayBtnRight[i] = button;
        }
    }

    private void fillLayoutLeft(final int rowsCount)
    {
        buttonsLeftGone();
        if (rowsCount <= 0) return;
        //lockOrientation.lock();
        int count = rowsCount;
        if (count > ROWS)
        {
            count = ROWS;
        }
        final z_RandomNumberGenerator generator = new z_RandomNumberGenerator(count, 0);
        final int finalCount = count;
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    int randIndex = generator.generate();
                    AppData.arrayBtnLeft[i].setText(list.get(randIndex).get_english());
                    AppData.arrayBtnLeft[i].setTextSize(textSize);
                    wordIndex = finalCount;
                }
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;

        for (int i = 0; i < count; i++)
        {
            Button button = (Button) linLayoutLeft.getChildAt(i);
            button.setVisibility(View.VISIBLE);
            button.setTranslationX(0);
            btnLeft_OnClick(button, i);
            AppData.arrayBtnLeft[i] = button;
        }
    }

    private String enWord = null;
    private Button btnNoRight = null;
    private void btnLeft_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int requestedOrientation = getActivity().getRequestedOrientation();
                if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                {
                    return;
                }
                btn_left_position = index;
                width = view.getWidth();
                height = view.getHeight();
                enWord = AppData.arrayBtnLeft[index].getText().toString();
                compareWords(spinnSelectedItem, enWord, ruWord);
                btnNoRight = (Button) view;
            }
        });
    }


    private String ruWord = null;
    private void btnRight_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int requestedOrientation = getActivity().getRequestedOrientation();
                if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                {
                    return;
                }
                btn_right_position = index;
                width = view.getWidth();
                height = view.getHeight();
                ruWord = AppData.arrayBtnRight[index].getText().toString();
                compareWords(spinnSelectedItem, enWord, ruWord);
                btnNoRight = (Button) view;
            }
        });
    }

    private Integer resultCompare = 0;
    private int guessedWordsCount = 0;
    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;
        if (enword != null && ruword != null)
        {
            lockOrientation.lock();
            DataBaseQueries.GetRowIdOfWordAsync asyncTask = new DataBaseQueries.GetRowIdOfWordAsync()
            {
                @Override
                public void resultAsyncTask(Integer id)
                {
                    resultCompare = id;
                    if (resultCompare > 0)
                    {
                        counterRightAnswer++;
                        guessedWordsCount++;
                        String text = AppData.arrayBtnLeft[btn_left_position].getText().toString();
                        a_SplashScreenActivity.speech.speak(text, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);
                        a_SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                        {
                            @Override
                            public void onStart(String utteranceId)
                            {

                            }

                            @Override
                            public void onDone(String utteranceId)
                            {

                            }

                            @Override
                            public void onError(String utteranceId)
                            {

                            }
                        });
                        enWord = null; ruWord = null;

                        animToRight = AppData.arrayBtnRight[btn_right_position].animate().x((width + delta))
                                    .setDuration(duration)
                                    .setInterpolator(new AccelerateDecelerateInterpolator());

                        ViewPropertyAnimator animToLeft = AppData.arrayBtnLeft[btn_left_position].animate().x(-(width + delta))
                                    .setDuration(duration)
                                    .setInterpolator(new AccelerateDecelerateInterpolator());
                            animToLeft_Listener(animToLeft);

                    }
                    if (resultCompare < 0)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), "Неправильно", Toast.LENGTH_SHORT).show();
                        counterRightAnswer--;
                        Animation animNotRight = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.anim_not_right);
                        animNotRight.setAnimationListener(new Animation.AnimationListener()
                        {
                            @Override
                            public void onAnimationStart(Animation animation)
                            {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation)
                            {

                                btnNoRight.setBackgroundResource(R.drawable.text_button_for_test);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation)
                            {

                            }
                        });
                        if (btnNoRight != null)
                        {
                            btnNoRight.setBackgroundResource(R.drawable.text_btn_for_test_red);
                            btnNoRight.startAnimation(animNotRight);
                        }
                    }
//                    boolean contains = arrStudiedDict.contains(spinnListDict.getSelectedItem());
//                    boolean containsInPlayList = a_MainActivity.getPlayList().contains(spinnListDict.getSelectedItem());
//                    if (counterRightAnswer == wordsCount && !contains && containsInPlayList)
//                    {
//                        arrStudiedDict.add(spinnListDict.getSelectedItem().toString());
//                        //counterRightAnswer = 0;
//                    }
                    //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            };
            asyncTask.execute(tableName, enword, ruword);
        }
    }

    private void updateArrayClickListenerLeft(Button[] newArray)
    {
        AppData.arrayBtnLeft = newArray;
        for (int i = 0; i < AppData.arrayBtnLeft.length; i++)
        {
            btnLeft_OnClick(AppData.arrayBtnLeft[i], i);
        }
        Button[] arrayBtn = AppData.arrayBtnLeft;
    }

    private void updateArrayClickListenerRight(Button[] newArray)
    {
        AppData.arrayBtnRight = newArray;
        for (int i = 0; i < AppData.arrayBtnRight.length; i++)
        {
            btnRight_OnClick(AppData.arrayBtnRight[i], i);
        }
        Button[] arrayBtn = AppData.arrayBtnRight;
    }


    private void animToLeft_Listener(ViewPropertyAnimator animator)
    {
        animator.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                //lockOrientation.lock();
                if (wordIndex >= wordsCount || wordsResidue <= 0)
                {
                    AppData.arrayBtnLeft[btn_left_position].setVisibility(View.INVISIBLE);
                    AppData.arrayBtnRight[btn_right_position].setVisibility(View.INVISIBLE);
                    if (guessedWordsCount == wordsCount)
                    {
                        ArrayList<String> list = new ArrayList<String>();
                        list.add(testResults.getOverallResult(counterRightAnswer, wordsCount));
                        list.add(counterRightAnswer + getString(R.string.text_out_of) + wordsCount);
                        //counterRightAnswer = 0;

                        //dialogTestComplete = new t_DialogTestComplete();
                        dialogTestComplete = t_DialogTestComplete.getInstance();
                        dialogTestComplete.setIDialogCompleteResult(t_MatchFragment.this);
                        Bundle bundle = new Bundle();
                        bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
                        bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
                        dialogTestComplete.setArguments(bundle);
                        dialogTestComplete.setCancelable(false);
                        dialogTestComplete.show(getFragmentManager(), "dialog_complete_lexicon");
                    }
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                else
                {
                    int randLeft = generatorLeft.generate() + ROWS;
                    int randRight = generatorRight.generate() + ROWS;
                    AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTaskLeft = new DataBaseQueries.GetWordsFromDBAsync()
                    {
                        @Override
                        public void resultAsyncTask(ArrayList<DataBaseEntry> list)
                        {
                            AppData.arrayBtnLeft[btn_left_position].setText(list.get(0).get_english());
                            AppData.arrayBtnLeft[btn_left_position].animate().translationX(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(null);
                            wordIndex++;
                            //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        }
                    };
                    asyncTaskLeft.execute(spinnSelectedItem, randLeft, randLeft);


                    AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTaskRight = new DataBaseQueries.GetWordsFromDBAsync()
                    {
                        @Override
                        public void resultAsyncTask(ArrayList<DataBaseEntry> list)
                        {
                            AppData.arrayBtnRight[btn_right_position].setText(list.get(0).get_translate());
                            ViewPropertyAnimator animToRight = AppData.arrayBtnRight[btn_right_position].animate().translationX(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator());
                            animToRight_Listener(animToRight);

                        }
                    };
                    asyncTaskRight.execute(spinnSelectedItem, randRight, randRight);

                }
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }

    @Override
    public void dialogCompleteResult(int res)
    {
        if (res == 0)
        {
            addToStudiedList();
            startTest(res);
        }
        if (res > 0)
        {
            addToStudiedList();
            int count = spinnListDict.getAdapter().getCount();
            int position = spinnListDict.getSelectedItemPosition();
            if (position < count)
            {
                position++;
                spinnListDict.setSelection(position);
            }
            if (position == count)
            {
                position = 0;
                spinnListDict.setSelection(position);
            }
        }
        if (res < 0)
        {
            addToStudiedList();

            spinnListDict.setSelection(-1);
            spinnSelectedIndex = -1;
            getActivity().onBackPressed();
            if (arrStudiedDict.size() > 0)
            {
                t_DialogChangePlayList dialogChangePlayList = new t_DialogChangePlayList();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(dialogChangePlayList.KEY_LIST_DICT, arrStudiedDict);
                dialogChangePlayList.setArguments(bundle);
                dialogChangePlayList.setCancelable(false);
                dialogChangePlayList.show(getFragmentManager(), "dialog_change_pl_lexicon");
            }
        }

    }

    private void addToStudiedList()
    {
        boolean containsInPlayList = false;
        for (p_ItemListDict item : a_MainActivity.getPlayList())
        {
            if (item.get_dictName().equals(spinnListDict.getSelectedItem()))
            {
                containsInPlayList = true; break;
            }
        }
        boolean contains = arrStudiedDict.contains(spinnListDict.getSelectedItem());
        if (counterRightAnswer == wordsCount && !contains && containsInPlayList)
        {
            arrStudiedDict.add(spinnListDict.getSelectedItem().toString());
            counterRightAnswer = 0;
        }
    }


    private void animToRight_Listener(ViewPropertyAnimator animator)
    {
        animator.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }

    private void animToDown_Listener(ViewPropertyAnimator animator, String side)
    {

    }

    private void animToTop_Listener(ViewPropertyAnimator animator, String side)
    {

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        spinnSelectedIndex = -1;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //region Получение размеров экрана
    private float getHeightScreen()
    {
        metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        int windowHeight = metrics.heightPixels;
        return windowHeight;
    }


}
