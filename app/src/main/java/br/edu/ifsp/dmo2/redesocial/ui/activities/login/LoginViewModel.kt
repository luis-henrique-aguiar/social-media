package br.edu.ifsp.dmo2.redesocial.ui.activities.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> get() = _password

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> get() = _passwordError

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        validateEmail(newEmail)
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        validatePassword(newPassword)
    }

    fun login() {
        val email = _email.value.orEmpty()
        val password = _password.value.orEmpty()

        if (!validateEmail(email) || !validatePassword(password)) {
            return
        }

        _isLoading.value = true
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _success.value = true
                } else {
                    _emailError.value = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos."
                        is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
                        else -> "Erro ao fazer login: ${task.exception?.message}"
                    }
                }
            }
    }

    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isBlank() -> {
                _emailError.value = "Preencha o campo de e-mail"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _emailError.value = "E-mail inválido"
                false
            }
            else -> {
                _emailError.value = null
                true
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        return when {
            password.isBlank() -> {
                _passwordError.value = "Preencha o campo de senha"
                false
            }
            else -> {
                _passwordError.value = null
                true
            }
        }
    }
}