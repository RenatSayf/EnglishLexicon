package com.myapp.lexicon.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseQueries;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class NewDictDialog extends DialogFragment
{
    public static final String TAG = "new_dict_dialog";
    private DataBaseQueries dataBaseQueries;
    private static NewDictDialog newDictDialog = null;
    private static INewDictDialogResult iNewDictDialogResult;
    public static NewDictDialog newInstance()
    {
        if (newDictDialog == null)
        {
            newDictDialog = new NewDictDialog();
        }
        return newDictDialog;
    }

    public interface INewDictDialogResult
    {
        void newDictDialogResult(@NonNull String dictName);
    }

    public void setNewDictDialogListener(INewDictDialogResult listener)
    {
        iNewDictDialogResult = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getActivity() != null)
        {
            final View view = getActivity().getLayoutInflater().inflate(R.layout.a_dialog_add_dict, new LinearLayout(getContext()), false);
            final EditText editText = view.findViewById(R.id.dialog_add_dict);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_new_dictionary).setIcon(R.drawable.icon_book)
                    .setPositiveButton(R.string.btn_text_add, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            String dictName = editText.getText().toString();
                            if (!dictName.equals(""))
                            {
                                try
                                {
                                    if (iNewDictDialogResult != null)
                                    {
                                        iNewDictDialogResult.newDictDialogResult(dictName);
                                    }
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    .setNegativeButton(R.string.btn_text_cancel, null)
                    .setView(view);

            editText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    String str = s.toString();
                    for (int i = 0; i < str.length(); i++)
                    {
                        int char_first = str.codePointAt(0);
                        if ((char_first >= 33 && char_first <= 64) || (char_first >= 91 && char_first <= 96) ||
                                (char_first >= 123 && char_first <= 126))
                        {
                            String str_wrong = str.substring(i);
                            editText.setText(str.replace(str_wrong,""));
                        }
                        if ((str.codePointAt(i) >= 33 && str.codePointAt(i) <= 47) || (str.codePointAt(i) >= 58 && str.codePointAt(i) <= 64) ||
                                (str.codePointAt(i) >= 91 && str.codePointAt(i) <= 96) || (str.codePointAt(i) >= 123 && str.codePointAt(i) <= 126))
                        {
                            String str_wrong = str.substring(i);
                            editText.setText(str.replace(str_wrong,""));
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s){}
            });

            return builder.create();
        } else
        {
            return super.onCreateDialog(null);
        }
    }
}
