package br.edu.ifsp.dmo2.redesocial.ui.activities.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _username = MutableLiveData("")
    val username: LiveData<String> get() = _username

    private val _fullName = MutableLiveData("")
    val fullName: LiveData<String> get() = _fullName

    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> get() = _usernameError

    private val _fullNameError = MutableLiveData<String?>()
    val fullNameError: LiveData<String?> get() = _fullNameError

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun updateUsername(username: String) {
        _username.value = username
    }

    fun updateFullName(fullName: String) {
        _fullName.value = fullName
    }

    fun register() {
        val username = _username.value.orEmpty()
        val fullName = _fullName.value.orEmpty()

        if (!validateFullName(fullName) || !validateUsername(username) || !isUsernameAvailable(username)) {
            return
        }



    }

    private fun validateFullName(fullName: String): Boolean {
        return when {
            fullName.isNotBlank() && fullName.all { it.isLetter() || it.isWhitespace() } -> {
                _fullNameError.value = "O nome deve ter apenas letras e espaços em branco"
                false
            }
            else -> {
                _fullNameError.value = null
                true
            }
        }
    }

    private fun validateUsername(username: String): Boolean {
        return when {
            Regex("^[A-Z-a-z0-9_]$").matches(username) -> {
                _usernameError.value = "O nome de usuário deve conter apenas letras, números e sublinados."
                false
            }
            else -> {
                _usernameError.value = null
                true
            }
        }
    }

    private fun isUsernameAvailable(username: String): Boolean {
        var result = true
        db.collection("usernames").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) result = false
            }
            .addOnFailureListener { e ->
                _usernameError.value = "Error ao verificar username: ${e.message}"
                result = false
            }
        return result
    }
}