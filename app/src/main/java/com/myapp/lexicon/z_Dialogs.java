package com.myapp.lexicon;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ренат on 22.09.2016.
 */
public class z_Dialogs extends Activity
{
    private static z_Dialogs ourInstance = new z_Dialogs();
    public IDialogCompleteResult iDialogCompleteResult;

    public static z_Dialogs getInstance()
    {
        return ourInstance;
    }

    private z_Dialogs()
    {
    }

    public interface IDialogCompleteResult
    {
        void dialogCompleteResult(int res);
    }

    public void setIDialogCompleteResult(IDialogCompleteResult result)
    {
        this.iDialogCompleteResult = result;
    }

    public void dialogComplete(Activity activity, ArrayList<String> params)
    {
        final Dialog dialogComplete;
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.t_dialog_complete_test, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("Завершено")
                .setCancelable(false)
                .setView(dialogView);
        dialogComplete = builder.create();
        dialogComplete.show();
        TextView textViewResult = (TextView) dialogView.findViewById(R.id.txt_view_result);
        if (params.size() > 0)
        {
            textViewResult.setText(params.get(0));
            if (params.get(0).equals(activity.getString(R.string.text_excellent)))
            {
                textViewResult.setTextColor(Color.GREEN);
            }
            if (params.get(0).equals(activity.getString(R.string.text_good)))
            {
                textViewResult.setTextColor(activity.getResources().getColor(R.color.colorOrange));
            }
            if (params.get(0).equals(activity.getString(R.string.text_bad)))
            {
                textViewResult.setTextColor(Color.RED);
            }
        }
        TextView textViewErrors = (TextView) dialogView.findViewById(R.id.txt_view_errors);
        if (params.size() > 1)
        {
            textViewErrors.setText(params.get(1));
        }
        ImageButton buttonNext = (ImageButton) dialogView.findViewById(R.id.btn_next);
        buttonNext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(1);
                }
                dialogComplete.dismiss();
            }
        });
        ImageButton buttonRepeat = (ImageButton) dialogView.findViewById(R.id.btn_repeat);
        buttonRepeat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(0);
                }
                dialogComplete.dismiss();
            }
        });
        ImageButton buttonComplete = (ImageButton) dialogView.findViewById(R.id.btn_complete);
        buttonComplete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(-1);
                }
                dialogComplete.dismiss();
            }
        });
    }

    public void dialogChangePlayList(Activity activity, final ArrayList<String> listDict)
    {
        if (listDict == null)
        {
            return;
        }
        boolean[]choice = new boolean[listDict.size()];
        final String[] items = new String[listDict.size()];
        for (int i = 0; i < listDict.size(); i++)
        {
            items[i] = listDict.get(i);
            choice[i] = true;
        }

        new AlertDialog.Builder(activity).setTitle("Исключить словари из списка воспроизведения?")
                .setMultiChoiceItems(items, choice, new DialogInterface.OnMultiChoiceClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        if (!isChecked)
                        {
                            items[which] = null;
                        }
                    }
                })
                .setPositiveButton("Да", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for (int i = 0; i < items.length; i++)
                        {
                            a_MainActivity.removeItemPlayList(items[i]);
                        }
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                }).create().show();
    }

}
