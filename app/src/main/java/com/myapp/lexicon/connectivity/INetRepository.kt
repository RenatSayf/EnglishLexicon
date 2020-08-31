package com.myapp.lexicon.connectivity

import io.reactivex.Observable
import org.w3c.dom.Document

interface INetRepository
{
    fun getTranslateDoc(url: String) : Observable<org.jsoup.nodes.Document>
}