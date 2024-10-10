package com.example.runpal.activities.account

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.models.User
import com.example.runpal.repositories.user.CombinedUserRepository
import com.example.runpal.repositories.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val combinedUserRepository: CombinedUserRepository,
    private val loginManager: LoginManager,
    @ApplicationContext private val context: Context
): ViewModel() {

    enum class State {
        LOADING, LOADED, FAILED
    }

    private val _state = MutableStateFlow(State.LOADING)
    private val _user = MutableStateFlow(User())

    val state = _state.asStateFlow()
    val user = _user.asStateFlow()

    private suspend fun loadUser() {
        val userId = loginManager.currentUserId()
        if (userId == null) _state.update { State.FAILED }
        else try {
            val user = combinedUserRepository.getUser(userId)
            _user.update { user }
            _state.update { State.LOADED }
        } catch (e: Exception) {
            _state.update { State.FAILED }
        }
    }
    init {
        viewModelScope.launch { loadUser() }
    }

    suspend fun update(name: String, last: String, weight: Double, profile: Uri?) {
        if (_state.value != State.LOADED) return
        try {
            val newUser = _user.value.copy(name = name, last = last, weight = weight)
            if (profile != null) newUser.profileUri = profile
            combinedUserRepository.update(newUser)
            loadUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}