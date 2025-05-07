package br.edu.ifsp.dmo2.redesocial.ui.activities.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> get() = _password

    private val _confirmPassword = MutableLiveData("")
    val confirmPassword: LiveData<String> get() = _confirmPassword

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> get() = _passwordError

    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError: LiveData<String?> get() = _confirmPasswordError

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        validateEmail(newEmail)
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        validatePassword(newPassword)
        validateConfirmPassword(_confirmPassword.value.orEmpty())
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        validateConfirmPassword(newConfirmPassword)
    }

    fun register() {
        val email = _email.value.orEmpty()
        val password = _password.value.orEmpty()
        val confirmPassword = _confirmPassword.value.orEmpty()

        if (!validateEmail(email) || !validatePassword(password) || validateConfirmPassword(confirmPassword)) {
            return
        }

        _isLoading.value = true
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _registerSuccess.value = true
                } else {
                    _emailError.value = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "E-mail já está em uso."
                        is FirebaseAuthWeakPasswordException -> "A senha é muito graca."
                        is FirebaseAuthInvalidCredentialsException -> "E-mail inválido"
                        else -> "Erro ao criar usuário: ${task.exception?.message}"
                    }
                }
            }
    }

    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isBlank() -> {
                _email.value = "Preencha o campo de e-mail"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _email.value = "E-mail inválido"
                false
            }
            else -> {
                _email.value = null
                true
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        return when {
            password.isBlank() -> {
                _passwordError.value = "Preencha a senha"
                false
            }
            password.length < 6 -> {
                _passwordError.value = "A senha deve ter pelo menos 6 caracteres."
                false
            }
            else -> {
                _passwordError.value = null
                true
            }
        }
    }

    private fun validateConfirmPassword(confirmPassword: String): Boolean {
        return when {
            confirmPassword.isBlank() -> {
                _confirmPasswordError.value = "Preencha o campo de confirmação de senha."
                false
            }
            confirmPassword != _password.value -> {
                _confirmPasswordError.value = "As senhas não são iguais"
                false
            }
            else -> {
                _confirmPasswordError.value = null
                true
            }
        }
    }
}