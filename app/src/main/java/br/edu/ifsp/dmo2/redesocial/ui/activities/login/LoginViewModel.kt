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

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun login(email: String, password: String) {
        if (!validateCredentials(email, password)) {
            _error.value = "Preencha todos os campos"
            return
        }

        if (!isValidEmail(email)) {
            _error.value = "E-mail inválido."
            return
        }

        if (!isValidPassword(password)) {
            _error.value = "A senha precisa ter 6 caracteres, 1 número e 1 dígito."
            return
        }

        _isLoading.value = true
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _success.value = true
                } else {
                    _error.value = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos."
                        is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
                        else -> "Erro ao fazer login: ${task.exception?.message}"
                    }
                }
            }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank();
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }
}