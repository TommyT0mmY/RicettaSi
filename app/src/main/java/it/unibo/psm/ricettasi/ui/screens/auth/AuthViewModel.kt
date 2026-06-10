package it.unibo.psm.ricettasi.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the login / sign-up form.
 *
 * Exposes submission state and error; email and password live in the UI state.
 */
class AuthViewModel(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Starts sign-in with the given credentials. */
    fun signIn(email: String, password: String) {
        if (!canSubmit(email, password)) return
        submit { sessionRepository.signIn(email.trim(), password) }
    }

    /** Starts sign-up for a new account. */
    fun signUp(email: String, password: String) {
        if (!canSubmit(email, password)) return
        submit { sessionRepository.signUp(email.trim(), password) }
    }

    /** Blocks submission when fields are invalid. */
    private fun canSubmit(email: String, password: String): Boolean =
        email.isNotBlank() && password.length >= 6

    /** Runs [action] inside a protected job: enables loading, catches errors. */
    private fun submit(action: suspend () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            try {
                action()
                _isSubmitting.value = false
            } catch (e: Exception) {
                _isSubmitting.value = false
                _errorMessage.value = e.message
            }
        }
    }
}