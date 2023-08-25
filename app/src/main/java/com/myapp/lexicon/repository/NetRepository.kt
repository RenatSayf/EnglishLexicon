package com.myapp.lexicon.repository

import com.myapp.lexicon.network.INetClient
import com.myapp.lexicon.network.NetClient

open class NetRepository(
    private val netClient: INetClient = NetClient()
) {

}