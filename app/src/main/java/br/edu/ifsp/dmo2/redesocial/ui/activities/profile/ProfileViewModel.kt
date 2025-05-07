package br.edu.ifsp.dmo2.redesocial.ui.activities.profile

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
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

    private val _selectedBitmap = MutableLiveData<Bitmap?>()
    val selectedBitmap: LiveData<Bitmap?> get() = _selectedBitmap

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    fun updateUsername(username: String) {
        _username.value = username
    }

    fun updateFullName(fullName: String) {
        _fullName.value = fullName
    }

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updateSelectedBitmap(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
    }

    fun register() {
        val username = _username.value.orEmpty()
        val fullName = _fullName.value.orEmpty()
        val email = _email.value.orEmpty()

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.email != email) {
            _usernameError.value = "Usuário não autenticado ou e-mail inválido."
            return
        }

        if (!validateFullName(fullName) || !validateUsername(username)) {
            return
        }

        _isLoading.value = true
        db.collection("usernames").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _usernameError.value = "Este username já está em uso."
                    _isLoading.value = false
                } else {
                    saveUserData(email, username, fullName)
                }
            }
            .addOnFailureListener { e ->
                _usernameError.value = "Erro ao verificar username: ${e.message}"
                _isLoading.value = false
            }
    }

    private fun saveUserData(email: String, username: String, fullName: String) {
        val profilePhotoString = convertImageToBase64()
        val data = hashMapOf(
            "fullName" to fullName,
            "username" to username,
            "profilePhoto" to profilePhotoString
        )

        db.runTransaction { transaction ->
            val usernameRef = db.collection("usernames").document(username)
            val userRef = db.collection("users").document(email)

            if (transaction.get(usernameRef).exists()) {
                throw FirebaseFirestoreException("Username já está em uso.", FirebaseFirestoreException.Code.ABORTED)
            }

            transaction.set(userRef, data)
            transaction.set(usernameRef, hashMapOf("email" to email))
        }.addOnSuccessListener {
            _registerSuccess.value = true
            _isLoading.value = false
        }.addOnFailureListener { e ->
            _usernameError.value = "Erro ao salvar dados: ${e.message}"
            _isLoading.value = false
        }
    }

    private fun validateFullName(fullName: String): Boolean {
        return when {
            fullName.isBlank() -> {
                _fullNameError.value = "Preencha o campo de nome."
                false
            }
            !fullName.all { it.isLetter() || it.isWhitespace() } -> {
                _fullNameError.value = "O nome deve conter paenas letras e espaços."
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
            username.isBlank() -> {
                _usernameError.value = "Preencha o campo de username."
                false
            }
            !Regex("^[A-Za-z0-9_]+$").matches(username) -> {
                _usernameError.value = "O nome de usuário deve conter apenas letras, números e sublinados."
                false
            }
            else -> {
                _usernameError.value = null
                true
            }
        }
    }

    private fun convertImageToBase64(): String {
        return _selectedBitmap.value?.let { bitmap ->
            Base64Converter.bitmapToString(bitmap)
        } ?: ""
    }

    fun clearErrors() {
        _usernameError.value = null
        _fullNameError.value = null
    }
}