package com.myapp.lexicon.repository.network

import io.ktor.client.HttpClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class MockNetRepository: NetRepository(HttpClient()) {

}