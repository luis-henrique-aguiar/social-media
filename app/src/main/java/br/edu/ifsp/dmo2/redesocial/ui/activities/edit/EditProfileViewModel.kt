package br.edu.ifsp.dmo2.redesocial.ui.activities.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.firestore

class EditProfileViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _fullName = MutableLiveData<String>("")
    val fullName: LiveData<String> get() = _fullName

    private val _currentPassword = MutableLiveData<String>("")
    val currentPassword: LiveData<String> get() = _currentPassword

    private val _newPassword = MutableLiveData<String>("")
    val newPassword: LiveData<String> get() = _newPassword

    private val _profilePicture = MutableLiveData<String?>(null)
    val profilePicture: LiveData<String?> get() = _profilePicture

    private val _fullNameError = MutableLiveData<String?>()
    val fullNameError: LiveData<String?> get() = _fullNameError

    private val _currentPasswordError = MutableLiveData<String?>()
    val currentPasswordError: LiveData<String?> get() = _currentPasswordError

    private val _newPasswordError = MutableLiveData<String?>()
    val newPasswordError: LiveData<String?> get() = _newPasswordError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _dataLoaded = MutableLiveData<Boolean>()
    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private val _success = MutableLiveData<Boolean>(false)
    val success: LiveData<Boolean> get() = _success

    init {
        loadUserData()
    }

    fun updateFullName(fullName: String) {
        _fullName.value = fullName
        validateFullName(fullName)
    }

    fun updateCurrentPassword(password: String) {
        _currentPassword.value = password
        validateCurrentPassword(password)
    }

    fun updateNewPassword(newPassword: String) {
        _newPassword.value = newPassword
        validateNewPassword(newPassword)
    }

    fun updateProfilePicture(profilePicture: String) {
        if (profilePicture.isNotEmpty()) {
            _profilePicture.value = profilePicture
        }
    }

    fun loadUserData() {
        val user = firebaseAuth.currentUser ?: return
        val email = user.email ?: return

        _isLoading.value = true
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                val fullName = document.getString("fullName") ?: ""
                _fullName.value = fullName
                _profilePicture.value = document.getString("profilePhoto")
                _isLoading.value = false
                _dataLoaded.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _fullNameError.value = "Erro ao carregar os dados do usuário: ${e.message}"
                _dataLoaded.value = false
            }
    }

    fun updateUser() {
        val user = firebaseAuth.currentUser ?: return
        val email = user.email ?: return
        val fullName = _fullName.value.orEmpty()
        val currentPassword = _currentPassword.value.orEmpty()
        val newPassword = _newPassword.value.orEmpty()
        val profilePicture = _profilePicture.value

        if (!validateFullName(fullName)) return
        if (newPassword.isNotEmpty()) {
            if (!validateCurrentPassword(currentPassword) || !validateNewPassword(newPassword)) return
            if (currentPassword == newPassword) {
                _newPasswordError.value = "A nova senha não pode ser igual à atual."
                return
            }
        }

        _isLoading.value = true
        if (newPassword.isEmpty()) {
            updateData(fullName, profilePicture)
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        updateData(fullName, profilePicture)
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        _newPasswordError.value = "Erro ao atualizar senha: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _currentPasswordError.value = when (e) {
                    is FirebaseAuthException -> {
                        when (e.errorCode) {
                            "ERROR_WRONG_PASSWORD" -> "Senha atual incorreta."
                            else -> "Erro ao autenticar: ${e.message}"
                        }
                    }
                    else -> "Erro ao autenticar: ${e.message}"
                }
            }
    }

    fun clearErrors() {
        _fullNameError.value = null
        _currentPasswordError.value = null
        _newPasswordError.value = null
    }

    private fun updateData(fullName: String, profilePicture: String?) {
        val user = firebaseAuth.currentUser ?: return
        val email = user.email ?: return

        _isLoading.value = true
        db.collection("users").document(email)
            .update(mapOf(
                "fullName" to fullName,
                "profilePhoto" to profilePicture
            ))
            .addOnSuccessListener {
                db.collection("posts")
                    .whereEqualTo("userEmail", email)
                    .get()
                    .addOnSuccessListener { posts ->
                        val batch = db.batch()
                        for (document in posts.documents) {
                            batch.update(document.reference, mapOf(
                                "fullName" to fullName,
                                "profilePhoto" to profilePicture
                            ))
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                _isLoading.value = false
                                _success.value = true
                                clearErrors()
                            }
                            .addOnFailureListener { e ->
                                _isLoading.value = false
                                _fullNameError.value = "Erro ao atualizar posts: ${e.message}"
                            }
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        _fullNameError.value = "Erro ao buscar posts: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _success.value = false
                _fullNameError.value = "Erro ao salvar dados: ${e.message}"
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

    private fun validateCurrentPassword(password: String): Boolean {
        return when {
            password.isBlank() -> {
                _currentPasswordError.value = "Preencha a senha"
                false
            }
            password.length < 6 -> {
                _currentPasswordError.value = "A senha deve ter pelo menos 6 caracteres."
                false
            }
            else -> {
                _currentPasswordError.value = null
                true
            }
        }
    }

    private fun validateNewPassword(password: String): Boolean {
        return when {
            password.isBlank() -> {
                _newPasswordError.value = "Preencha a senha"
                false
            }
            password.length < 6 -> {
                _newPasswordError.value = "A senha deve ter pelo menos 6 caracteres."
                false
            }
            else -> {
                _newPasswordError.value = null
                true
            }
        }
    }
}