package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo2.redesocial.model.Post
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LiveData

data class UserData(
    val username: String = "Usuário",
    val email: String = "",
    val profilePhoto: Bitmap? = null,
    val fullName: String
)

class HomeViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> get() = _userData

    private val _postImage = MutableLiveData<String?>()
    val postImage: LiveData<String?> get() = _postImage

    private val _postDescription = MutableLiveData<String>()
    val postDescription: LiveData<String> get() = _postDescription

    private val _location = MutableLiveData<String>("")
    val location: LiveData<String> get() = _location

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        loadUserData()
    }

    fun updateLocation(location: String) {
        _location.value = location
    }

    private fun loadUserData() {
        val user = firebaseAuth.currentUser ?: return
        val email = user.email ?: return

        _isLoading.value = true
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Usuário"
                val fullName = document.getString("fullName") ?: "Usuário"
                val imageString = document.getString("profilePhoto")
                val bitmap = imageString?.let { Base64Converter.stringToBitmap(it) }
                _userData.value = UserData(username, email, bitmap, fullName)
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Erro ao carregar dados do usuário: ${e.message}"
                _isLoading.value = false
            }
    }

    fun addPost(image: String?, description: String) {
        val user = firebaseAuth.currentUser ?: return
        val email = user.email ?: return

        _isLoading.value = true
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                val fullName = document.getString("fullName") ?: "Usuário"
                val profilePhoto = document.getString("profilePhoto")
                val post = mapOf(
                    "description" to description,
                    "image" to image,
                    "fullName" to fullName,
                    "profilePhoto" to profilePhoto,
                    "location" to _location.value,
                    "userEmail" to email
                )

                db.collection("posts").add(post)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        loadPosts()
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Erro ao adicionar post: ${e.message}"
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Erro ao carregar dados do usuário: ${e.message}"
                _isLoading.value = false
            }
    }

    fun loadPosts() {
        _isLoading.value = true
        val posts = mutableListOf<Post>()

        db.collection("posts").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    _isLoading.value = false
                    _posts.value = emptyList()
                    return@addOnSuccessListener
                }

                for (document in documents.documents) {
                    val imageString = document.getString("image")
                    val description = document.getString("description") ?: ""
                    val email = document.getString("userEmail")
                    val fullName = document.getString("fullName")
                    val profilePhotoString = document.getString("profilePhoto")
                    val location = document.getString("location")

                    if (email != null && fullName != null) {
                        val postBitmap = try {
                            imageString?.let { Base64Converter.stringToBitmap(it) }
                        } catch (e: Exception) {
                            null
                        }

                        val userProfilePhoto = try {
                            profilePhotoString?.let { Base64Converter.stringToBitmap(it) } ?: createBitmap(1, 1)
                        } catch (e: Exception) {
                            createBitmap(1, 1)
                        }

                        posts.add(
                            Post(
                                description = description,
                                photo = postBitmap,
                                fullName = fullName,
                                userProfilePhoto = userProfilePhoto,
                                location = location,
                                userEmail = email
                            )
                        )
                    }
                    _posts.value = posts
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Erro ao carregar posts: ${e.message}"
                _isLoading.value = false
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}