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
import com.myapp.lexicon.main.MainActivity;
import com.myapp.lexicon.settings.SettingsExtKt;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;


/** @noinspection CodeBlock2Expr*/
public class AboutAppFragment extends Fragment
{
    private View fragment_view = null;

    public AboutAppFragment()
    {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (fragment_view == null)
        {
            fragment_view = inflater.inflate(R.layout.fragment_about_app, container, false);
        }

        Toolbar toolBar = fragment_view.findViewById(R.id.tool_bar);
        toolBar.setNavigationOnClickListener( v -> getParentFragmentManager().popBackStack());

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
        buttonEvaluate.setOnClickListener( view -> {
            SettingsExtKt.goToAppStore(requireContext());
        });

        final TextView tvLinkPrivacyPolicy = fragment_view.findViewById(R.id.tvLinkPrivacyPolicy);
        tvLinkPrivacyPolicy.setOnClickListener( view -> {
            tvLinkPrivacyPolicy.setTextColor(Color.RED);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_link)));
            startActivity(intent);
        });

        return fragment_view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MainActivity activity = (MainActivity) requireActivity();
        activity.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                activity.getSupportFragmentManager().popBackStack();
                this.remove();
            }
        });
    }
}
