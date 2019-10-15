package com.myapp.lexicon.connectivity;

import android.content.Context;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseQueries;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Random;

public class TranslateApi
{
    private Context context;
    private DataBaseQueries dataBaseQueries;
    private String[] apiKeys = new String[]
            {
                    "trnsl.1.1.20160128T151725Z.d4bbd1b06137bfde.2e1323688363a820f730629556d68f2b9f0a1a19",
                    "trnsl.1.1.20160607T122324Z.85a9ab6b9e12baac.f5b66628231eb6175c5cf1393d1601c2cfb5d553",
                    "trnsl.1.1.20170126T141135Z.59c382f4c439bbaf.3226485ac0b3691894625bddb3c48c74f5067706",
                    "trnsl.1.1.20170423T121414Z.3240e7971183355e.e2d03076a138b1ee0324ae8e6db4aa072ddc3390",
                    "trnsl.1.1.20170423T121504Z.806c486ec21aa245.7d9eba215a558252d45d446cf7c9ace081ee2a97",
                    "trnsl.1.1.20170423T121559Z.11399a044f857e17.8a31fb51a77ca0877235350e67334b587a5eee75",
                    "trnsl.1.1.20170423T121720Z.23f1ac5940850aeb.41f79bbea45ddc51abaf7482e12584fba14798b4",
                    "trnsl.1.1.20180509T102655Z.763ae7eb3667e3db.6af5b43528f55e15c2d0f1180495e58d4f41f23e",
                    "trnsl.1.1.20180509T102734Z.dbead781c0030547.74f87c9d5d547bfa668c19750720d09aa4b85dfe",
                    "trnsl.1.1.20180509T102810Z.084fc45ba7e93720.f83f7d103835eaae205f6644bdb8adbb94a5dd80"
            };
    private Random generator;

    public TranslateApi(Context context)
    {
        this.context = context;
        dataBaseQueries = new DataBaseQueries(context);
        generator = new Random(new Date().getTime());
    }

    public String getStringUrl(String enWord)
    {
        String trans_direct = this.context.getString(R.string.translate_direct_en_ru);
        String enWordEncode;
        try
        {
            enWordEncode = URLEncoder.encode(enWord, "utf-8");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
        String apiKey = dataBaseQueries.getApiKey();
        if (apiKey.equals(""))
        {
            apiKey = apiKeys[generator.nextInt(apiKeys.length)];
        }
        return "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + apiKey + "&text=" + enWordEncode + "&lang=" + trans_direct + "&[format=plain]&[options=1]";
    }
}
