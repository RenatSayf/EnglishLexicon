package com.myapp.lexicon.connectivity

import io.reactivex.Observable
import org.jsoup.Jsoup
import java.io.IOException

class NetRepositoryImpl : INetRepository
{
    override fun getTranslateDoc(url: String): Observable<org.jsoup.nodes.Document>
    {
        return Observable.create { emitter ->
            try
            {
                val document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36")
                        .get()
                val body = document.body()
                val text = body.select("#dictionary > div.dictionary-list.tab-content > div").text()
                emitter.onNext(document)
            }
            catch (e: IOException)
            {
                emitter.onError(e)
            }
            finally
            {
                emitter.onComplete()
            }
        }
    }
}