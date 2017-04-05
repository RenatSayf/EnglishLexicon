package com.myapp.lexicon.addword;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.helpers.RandomNumberGenerator;

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
import java.util.Date;
import java.util.Random;

/**
 * Created by Renat on 20.03.2017.
 */

public class GetTranslateLoader extends AsyncTaskLoader
{
    public static final String KEY_TEXT_ENTERED = "text_entered";

    //private Context context;
    private String langSystem;
    private String textEntered;
    private DataBaseQueries dataBaseQueries;
    private String[] apiKeys = new String[]
            {
                    "trnsl.1.1.20160128T151725Z.d4bbd1b06137bfde.2e1323688363a820f730629556d68f2b9f0a1a19",
                    "trnsl.1.1.20160607T122324Z.85a9ab6b9e12baac.f5b66628231eb6175c5cf1393d1601c2cfb5d553",
                    "trnsl.1.1.20170126T141135Z.59c382f4c439bbaf.3226485ac0b3691894625bddb3c48c74f5067706"
            };
    private Random generator;

    public GetTranslateLoader(Context context, Bundle bundle)
    {
        super(context);
        //this.context = context;
        if (bundle != null)
        {
            textEntered = bundle.getString(KEY_TEXT_ENTERED);
        }
        dataBaseQueries = new DataBaseQueries(context);
        generator = new Random(new Date().getTime());
    }

    @Override
    public Object loadInBackground()
    {
        String result = getContentFromTranslator(textEntered);
        return getWordFromJson(result);
    }

    private String getContentFromTranslator(String text)
    {
        String lang = getLangTranslate(text)[0];
        String ui = getLangTranslate(text)[1];
        String undefined = "{\"text\":[\"undefined\"]}";
        if (lang == null || ui == null)
        {
            return undefined;
        }

        String text_encode = null;
        try
        {
            text_encode = URLEncoder.encode(text,"utf-8");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        String format = "plain";
        //String keyApi = "trnsl.1.1.20160607T122324Z.85a9ab6b9e12baac.f5b66628231eb6175c5cf1393d1601c2cfb5d553";
        String apiKey = dataBaseQueries.getApiKey();
        if (apiKey.equals(""))
        {
            apiKey = apiKeys[generator.nextInt(apiKeys.length)];
        }
        String link = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + apiKey + "&text=" + text_encode + "&lang=" + lang + "&[format=" + format + "]&[options=1]";

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
            String line;
            while ((line=reader.readLine()) != null)
            {
                buf.append(line).append("\n");
            }
            return(buf.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                    e.printStackTrace();
                }
            }
        }
    }

    private String[] getLangTranslate(String text)
    {
        String[] lang = new String[2];
        for (int i = 0; i < text.length(); i++)
        {
            int char_first = text.codePointAt(0);
            if ((char_first >= 33 && char_first <= 64) || (text.codePointAt(i) >= 91 && text.codePointAt(i) <= 96) || (text.codePointAt(i) >= 123 && text.codePointAt(i) <= 126))
            {
                continue;
            }
            if (text.codePointAt(i) >= 1025 && text.codePointAt(i) <= 1105)
            {
                lang[0] = "ru-en";
                lang[1] = "ru";
            }
            else if (text.codePointAt(i) >= 65 && text.codePointAt(i) <= 122)
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
    private ArrayList<String> getWordFromJson(String json_str)
    {
        ArrayList<String> list = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray text;
        try
        {
            jsonObject = new JSONObject(json_str);
            text = jsonObject.getJSONArray("text");
            String code = jsonObject.getString("code");
            for (int i = 0; i < text.length(); i++)
            {
                String str = text.getString(i);
                list.add(str);
            }
            list.add(0, code);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return list;
    }

}
