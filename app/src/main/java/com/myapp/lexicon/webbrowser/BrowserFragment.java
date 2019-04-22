package com.myapp.lexicon.webbrowser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.dialogs.TranslatorDialog;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BrowserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BrowserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BrowserFragment extends Fragment
{
    public static final String TAG = "browser_fragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CoordinatorLayout coordinatorLayout;
    private LexiconWebView webView;
    private String selectedText;

    private OnFragmentInteractionListener mListener;

    public BrowserFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BrowserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BrowserFragment newInstance(String param1, String param2)
    {
        BrowserFragment fragment = new BrowserFragment();
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragment_view = inflater.inflate(R.layout.browser_fragment, container, false);
        coordinatorLayout = fragment_view.findViewById(R.id.web_view_coord_laytout);
        FloatingActionButton transFloatBtn = fragment_view.findViewById(R.id.translate_fab);
        transFloatBtnOnClick(transFloatBtn);
        webView = fragment_view.findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new LexiconWebClient());
        webView.loadUrl("https://www.bbc.com/");

        webView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                view.performClick();
                if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_POINTER_UP)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    {
                        webView.evaluateJavascript("(function(){\n" +
                                        "    if(window.getSelection().toString() !== \"\"){\n" +
                                        "        return window.getSelection().toString();\n" +
                                        "    }\n" +
                                        "    else{\n" +
                                        "        return null;\n" +
                                        "    }\n" +
                                        "})()",
                                new ValueCallback<String>()
                                {
                                    @Override
                                    public void onReceiveValue(String value)
                                    {
                                        selectedText = value;
                                    }
                                });
                    }
                }
                return false;
            }
        });
        return fragment_view;
    }

    private void transFloatBtnOnClick(FloatingActionButton transFloatBtn)
    {
        transFloatBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (selectedText != null && !selectedText.equals("null"))
                {
                    String strValue = selectedText.replaceAll("\"\\b", "").replaceAll("\"\\B", "");
                    TranslatorDialog translatorDialog = TranslatorDialog.getInstance(strValue, new TranslatorDialog.NoticeDialogListener()
                    {
                        @Override
                        public void onDialogAddClick(AppCompatDialogFragment dialog)
                        {
                            webView.clearFocus();
                            dialog.dismiss();
                        }

                        @Override
                        public void onDialogCancelClick(AppCompatDialogFragment dialog)
                        {
                            webView.clearFocus();
                            dialog.dismiss();
                        }
                    });
                    if (getFragmentManager() != null)
                    {
                        translatorDialog.show(getFragmentManager(), TranslatorDialog.TAG);
                    }
                }
            }
        });
    }

    public WebView getLexiconWebView()
    {
        return webView;
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
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
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


}
