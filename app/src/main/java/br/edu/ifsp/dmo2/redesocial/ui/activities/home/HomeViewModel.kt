package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo2.redesocial.model.Post
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserData(
    val username: String = "Usuário",
    val email: String = "",
    val profilePhoto: Bitmap? = null
)

class HomeViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> get() = _userData

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            _error.value = "Usuário não autenticado."
            return
        }

        _isLoading.value = true
        val email = user.email.orEmpty()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                val username = document.data?.get("username")?.toString() ?: "Usuário"
                val imageString = document.data?.get("profilePhoto")?.toString()
                val bitmap = if (!imageString.isNullOrEmpty()) {
                    Base64Converter.stringToBitmap(imageString)
                } else {
                    null
                }
                _userData.value = UserData(username, email, bitmap)
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Errro ao carregar dados do usuário: ${e.message}"
                _isLoading.value = false
            }
    }

    fun loadPosts() {
        _isLoading.value = true
        db.collection("posts").get()
            .addOnSuccessListener { result ->
                val posts = mutableListOf<Post>()
                for (document in result.documents) {
                    val imageString = document.data?.get("image")?.toString()
                    val description = document.data?.get("description")?.toString() ?: ""
                    val bitmap = imageString?.let { Base64Converter.stringToBitmap(it) }
                    if (bitmap != null) {
                        posts.add(Post(description, bitmap))
                    }
                }
                _posts.value = posts
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Error ao carregar posts: ${e.message}"
                _isLoading.value = false
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}