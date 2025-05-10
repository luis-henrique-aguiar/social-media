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
        val user = firebaseAuth.currentUser
        if (user == null) {
            _error.value = "Usuário não autenticado."
            return
        }

        _isLoading.value = true
        val email = user.email.orEmpty()
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
        if (image == null) {
            _error.value = "Erro ao carregar a imagem"
            return
        }

        val user = firebaseAuth.currentUser
        if (user == null) {
            _error.value = "Usuário não autenticado."
            return
        }

        val postData = hashMapOf(
            "image" to image,
            "description" to description,
            "userEmail" to user.email,
            "fullName" to (_userData.value?.fullName ?: "Usuário"),
            "username" to (_userData.value?.username ?: "Usuário"),
            "location" to _location.value
        )

        _isLoading.value = true
        db.collection("posts").add(postData)
            .addOnSuccessListener {
                loadPosts()
            }
            .addOnFailureListener { e ->
                _error.value = "Erro ao realizar o post: ${e.message}"
                _isLoading.value = false
            }
    }

    fun loadPosts() {
        _isLoading.value = true
        val postsList = mutableListOf<Post>()

        db.collection("posts").get()
            .addOnSuccessListener { postsResult ->
                if (postsResult.isEmpty) {
                    _posts.value = emptyList()
                    _isLoading.value = false
                    return@addOnSuccessListener
                }

                var completedQueries = 0
                val totalQueries = postsResult.documents.size

                for (document in postsResult.documents) {
                    val imageString = document.getString("image")
                    val description = document.getString("description") ?: ""
                    val userEmail = document.getString("userEmail")
                    val fullName = document.getString("fullName") ?: "Usuário"
                    val location = document.getString("location")

                    if (imageString == null || userEmail == null) {
                        completedQueries++
                        if (completedQueries == totalQueries) {
                            _posts.value = postsList
                            _isLoading.value = false
                        }
                        continue
                    }

                    val postBitmap = Base64Converter.stringToBitmap(imageString)
                    if (postBitmap == null) {
                        completedQueries++
                        if (completedQueries == totalQueries) {
                            _posts.value = postsList
                            _isLoading.value = false
                        }
                        continue
                    }

                    db.collection("users").document(userEmail).get()
                        .addOnSuccessListener { userDocument ->
                            val profilePhotoString = userDocument.getString("profilePhoto")
                            val userProfilePhoto = profilePhotoString?.let {
                                Base64Converter.stringToBitmap(it)
                            } ?: createBitmap(1, 1)

                            postsList.add(
                                Post(
                                    description = description,
                                    photo = postBitmap,
                                    fullName = fullName,
                                    userProfilePhoto = userProfilePhoto,
                                    location = location
                                )
                            )

                            completedQueries++
                            if (completedQueries == totalQueries) {
                                _posts.value = postsList
                                _isLoading.value = false
                            }
                        }
                        .addOnFailureListener { e ->
                            postsList.add(
                                Post(
                                    description = description,
                                    photo = postBitmap,
                                    fullName = fullName,
                                    userProfilePhoto = createBitmap(1, 1),
                                    location = null
                                )
                            )

                            completedQueries++
                            if (completedQueries == totalQueries) {
                                _posts.value = postsList
                                _isLoading.value = false
                            }
                            _error.value = "Erro ao carregar foto de perfil: ${e.message}"
                        }
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