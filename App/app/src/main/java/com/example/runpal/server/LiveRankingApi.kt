package com.example.runpal.server

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.runpal.LIVE_RANKING_ADDRESS
import com.example.runpal.activities.running.NonlocalRunState
import com.example.runpal.models.EventResult
import com.example.runpal.models.Run
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.lang.reflect.Type
import java.time.Duration

class LiveRankingApi @AssistedInject constructor(
    @AuthorizedClient client: OkHttpClient,
    @Assisted val eventID: String
) {
    enum class State {
        READY,
        LISTENING,
        RESET,
        STOPPED
    }
    private var internalState: State = State.READY
    private val scope = CoroutineScope(Dispatchers.Main)
    private val listener: EventSourceListener = object: EventSourceListener() {
        val listType: Type = object : TypeToken<List<EventResult>>() {}.type
        override fun onEvent(
            eventSource: EventSource,
            id: String?,
            type: String?,
            data: String
        ) {
            if (data.startsWith("[")) {
               val update: List<EventResult> = Gson().fromJson(data, listType)
                scope.launch { _state.value =  update}
            }
        }
        override fun onFailure(
            eventSource: EventSource,
            t: Throwable?,
            response: Response?
        ) {
            super.onFailure(eventSource, t, response)
            t?.printStackTrace()
            scope.launch {
                if (internalState == State.LISTENING) {
                    internalState = State.RESET
                    delay(10000)
                    if (internalState == State.RESET)
                        start()
                }
            }
        }

        override fun onOpen(eventSource: EventSource, response: Response) {
            super.onOpen(eventSource, response)
        }

        override fun onClosed(eventSource: EventSource) {
            super.onClosed(eventSource)
        }
    }

    private val client: OkHttpClient = client.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(20))
        .writeTimeout(Duration.ofSeconds(20))
        .build()
    private val request = Request.Builder()
        .url(LIVE_RANKING_ADDRESS + eventID)
        .header("Accept", "text/event-stream")
        .build()
    private val _state = mutableStateOf(listOf<EventResult>())
    val state: List<EventResult>
        get() = _state.value


    var source: EventSource? = null
    fun start() {
        if (internalState != State.READY) return
        internalState = State.LISTENING
        createSource()
    }
    fun stop() {
        source?.cancel()
        source = null
        internalState = State.STOPPED
    }
    private fun createSource() {
        source = EventSources.createFactory(client).newEventSource(request, listener)
    }


}

@AssistedFactory
interface LiveRankingApiFactory {
    fun createLiveRankingApi(eventID: String): LiveRankingApi
}