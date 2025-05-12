package br.edu.ifsp.dmo2.redesocial.ui.activities.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo2.redesocial.ui.utils.Validator
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

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun register() {
        val email = _email.value.orEmpty()
        val password = _password.value.orEmpty()
        val confirmPassword = _confirmPassword.value.orEmpty()

        if (!validateEmail(email) || !validatePassword(password) || !validateConfirmPassword(confirmPassword)) {
            return
        }

        _isLoading.value = true
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _registerSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "E-mail já está em uso."
                        is FirebaseAuthWeakPasswordException -> "A senha é muito fraca."
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
        _emailError.value = Validator.validateEmail(email)
        return _emailError.value == null
    }

    private fun validatePassword(password: String): Boolean {
        _passwordError.value = Validator.validatePassword(password)
        return _passwordError.value == null
    }

    private fun validateConfirmPassword(confirmPassword: String): Boolean {
        _confirmPasswordError.value = Validator.validateConfirmPassword(_password.value.orEmpty(), confirmPassword)
        return _confirmPasswordError.value == null
    }
}