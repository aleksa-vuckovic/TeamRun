package com.example.runpal.activities.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.ACTION_CURRENT_REMINDER
import com.example.runpal.ACTION_DAILY_REMINDER
import com.example.runpal.EVENT_ID_KEY
import com.example.runpal.EventReminderReceiver
import com.example.runpal.R
import com.example.runpal.REMINDER_REQUEST_CODE
import com.example.runpal.ServerException
import com.example.runpal.models.Event
import com.example.runpal.repositories.ServerEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EventViewModel @Inject constructor(
    private val serverEventRepository: ServerEventRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
): ViewModel() {

    enum class State {
        LOADING, LOADED, ERROR
    }

    private val id: String = savedStateHandle[EVENT_ID_KEY]!!

    private val _state = mutableStateOf(State.LOADING)
    private val _event = mutableStateOf(Event())

    val state: State
        get() = _state.value
    val event: Event
        get() = _event.value


    init {
        reload()
    }

    fun reload() {
        _state.value = State.LOADING
        viewModelScope.launch {
            try {
                _event.value = serverEventRepository.data(id)
                _state.value = State.LOADED
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                _state.value = State.ERROR
            }
        }
    }

    fun follow() {
        viewModelScope.launch {
            try {
                serverEventRepository.follow(id)
                _event.value = _event.value.copy(following = true, followers = _event.value.followers + 1)
            } catch (_: ServerException) {}
            catch (e: Exception) {
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
            }

            val till = _event.value.time - System.currentTimeMillis()
            if (till > 0L && till < 24*3600000L) {
                //Set a reminder
                val reminderIntent = Intent(context, EventReminderReceiver::class.java)
                reminderIntent.action = ACTION_CURRENT_REMINDER
                val reminder = PendingIntent.getBroadcast(context, REMINDER_REQUEST_CODE, reminderIntent, PendingIntent.FLAG_IMMUTABLE)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val triggerAtMillis = _event.value.time - 15*3600000L
                alarmManager.set(AlarmManager.RTC, triggerAtMillis, reminder)
            }
        }
    }
    fun unfollow() {
        viewModelScope.launch {
            try {
                serverEventRepository.unfollow(id)
                _event.value = _event.value.copy(following = false, followers = _event.value.followers - 1)
            } catch (_: ServerException) {}
            catch (e: Exception) {
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
            }
        }
    }


}