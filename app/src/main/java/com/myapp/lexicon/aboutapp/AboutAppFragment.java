package com.myapp.lexicon.aboutapp;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.myapp.lexicon.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class AboutAppFragment extends Fragment
{
    private View fragment_view = null;

    public AboutAppFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (fragment_view == null)
        {
            fragment_view = inflater.inflate(R.layout.fragment_about_app, container, false);
        }

        TextView versionNameTV = fragment_view.findViewById(R.id.version_name_tv);
        try
        {
            if (getActivity() != null)
            {
                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                versionNameTV.setText("v.".concat(packageInfo.versionName));
            }
        } catch (PackageManager.NameNotFoundException e)
        {
            versionNameTV.setText("???");
        }

        Button buttonEvaluate = fragment_view.findViewById(R.id.btn_evaluate);
        buttonEvaluate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.app_link)));
                startActivity(intent);
            }
        });

        final TextView tvLinkPrivacyPolicy = fragment_view.findViewById(R.id.tvLinkPrivacyPolicy);
        tvLinkPrivacyPolicy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                tvLinkPrivacyPolicy.setTextColor(Color.RED);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)));
                startActivity(intent);
            }
        });

        return fragment_view;
    }

}
