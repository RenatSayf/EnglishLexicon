package com.myapp.lexicon.service;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.myapp.lexicon.R;

/**
 * Created by Renat
 */

public class ServiceDialog extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_dialog_activity);

        ModalFragment modalFragment = ModalFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, modalFragment).commit();
    }
}
