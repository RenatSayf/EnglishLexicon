package com.myapp.lexicon.addword;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myapp.lexicon.helpers.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Ренат on 16.06.2016.
 */
public class OnlineTranslatorApi
{
    private String keyApi = "trnsl.1.1.20160607T122324Z.85a9ab6b9e12baac.f5b66628231eb6175c5cf1393d1601c2cfb5d553";
    private TextView textView;
    private ProgressBar progressBar;
    private String langSystem;
    private String undefined = "{\"text\":[\"неопределено\"]}";

    public OnlineTranslatorApi(TextView textView, ProgressBar progressBar)
    {
        this.textView = textView;
        this.progressBar = progressBar;
    }

    public void getTranslateAsync(final String text)
    {
        AsyncTask asyncTask = new AsyncTask()
        {
            @Override
            protected void onPreExecute()
            {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Object[] params)
            {
                String _content = null;
                try
                {
                    _content = getContentFromTranslator(text);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    MyLog.v("Исключение - " + e.getMessage());
                }
                return _content;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                progressBar.setVisibility(View.GONE);
                ArrayList<String> list = getWord(o.toString());
                if (list.size() > 0)
                {
                    textView.setText(list.get(0));
                }
                MyLog.v("str = "+list.get(0));

            }
        };
        asyncTask.execute();
    }
    private String getContentFromTranslator(String text)
    {
        String lang = getLangTranslate(text)[0];
        String ui = getLangTranslate(text)[1];
        if (lang == null || ui == null)
        {
            return undefined;
        }
        MyLog.v("lang = " + lang + "    ui = " + ui);

        String text_encode = null;
        try
        {
            text_encode = URLEncoder.encode(text,"utf-8");
        } catch (UnsupportedEncodingException e)
        {
            MyLog.v("Исключение = " + e.getMessage());
        }

        String format = "plain";
        String link = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + keyApi + "&text=" + text_encode + "&lang=" + lang + "&[format=" + format + "]&[options=1]";

        BufferedReader reader = null;
        try
        {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept","*/*");
            connection.setRequestProperty("Accept-Encoding","utf-8");
            connection.setRequestProperty("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36");

            connection.setDoOutput(true);
            connection.setReadTimeout(100000);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder buf=new StringBuilder();
            String line=null;
            while ((line=reader.readLine()) != null)
            {
                buf.append(line + "\n");
            }
            return(buf.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            MyLog.v("Исключение - " + e.getMessage());
            return undefined;
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (Exception e)
                {
                    MyLog.v("Иключение - " + e.getMessage());
                }
            }
        }
    }

    private String[] getLangTranslate(String text)
    {
        String str = text;
        String[] lang = new String[2];
        for (int i = 0; i < str.length(); i++)
        {
            int char_first = str.codePointAt(0);
            if ((char_first >= 33 && char_first <= 64) || (str.codePointAt(i) >= 91 && str.codePointAt(i) <= 96) || (str.codePointAt(i) >= 123 && str.codePointAt(i) <= 126))
            {
                continue;
            }
            if (str.codePointAt(i) >= 1025 && str.codePointAt(i) <= 1105)
            {
                lang[0] = "ru-en";
                lang[1] = "ru";
            }
            else if (str.codePointAt(i) >= 65 && str.codePointAt(i) <= 122)
            {
                lang[0] = "en-ru";
                lang[1] = "en";
            }
            else
            {
                lang[0] = null;
                lang[1] = null;
            }
        }
        return lang;
    }
    private ArrayList<String> getWord(String json_str)
    {
        ArrayList<String> list = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray text;
        try
        {
            jsonObject = new JSONObject(json_str);
            text = jsonObject.getJSONArray("text");
            for (int i = 0; i < text.length(); i++)
            {
                String str = text.getString(i);
                list.add(str);
            }
        } catch (JSONException e)
        {
            MyLog.v("Исключени - "+e.getMessage());
        }

        return list;
    }
}
