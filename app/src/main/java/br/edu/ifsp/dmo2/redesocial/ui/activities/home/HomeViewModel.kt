package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo2.redesocial.model.Post
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import androidx.core.graphics.createBitmap

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

    private val _posts = MutableLiveData<List<Post>>(emptyList())
    val posts: LiveData<List<Post>> get() = _posts

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isLastPage = MutableLiveData<Boolean>(false)
    val isLastPage: LiveData<Boolean> get() = _isLastPage

    private var lastDocument: DocumentSnapshot? = null

    init {
        loadUserData()
    }

    fun updateLocation(location: String) {
        _location.value = location
    }

    private fun loadUserData() {
        val user = firebaseAuth.currentUser ?: run {
            _error.value = "Usuário não autenticado"
            return
        }
        val email = user.email ?: run {
            _error.value = "Email do usuário não encontrado"
            return
        }

        _isLoading.value = true
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Usuário"
                val fullName = document.getString("fullName") ?: "Usuário"
                val imageString = document.getString("profilePhoto")
                val bitmap = imageString?.let { Base64Converter.stringToBitmap(it) }
                _userData.value = UserData(username, email, bitmap, fullName)
                _isLoading.value = false
                loadPosts()
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
                    "userEmail" to email,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("posts").add(post)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        resetPagination()
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
        db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                try {
                    val newPosts = processDocuments(documents)
                    _posts.value = newPosts
                    lastDocument = if (documents.isEmpty) null else documents.documents.last()
                    _isLastPage.value = documents.isEmpty || documents.size() < 5
                    _isLoading.value = false
                } catch (e: Exception) {
                    _error.value = "Erro ao processar posts"
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Erro ao carregar posts: ${e.message}"
                _isLoading.value = false
            }
    }

    fun loadMorePosts() {
        if (_isLastPage.value == true || _isLoading.value == true || lastDocument == null) {
            return
        }

        _isLoading.value = true
        db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .startAfter(lastDocument!!)
            .get()
            .addOnSuccessListener { documents ->
                try {
                    val currentPosts = _posts.value ?: emptyList()
                    val newPosts = processDocuments(documents)
                    _posts.value = currentPosts + newPosts
                    lastDocument = if (documents.isEmpty) null else documents.documents.last()
                    _isLastPage.value = documents.isEmpty || documents.size() < 5
                    _isLoading.value = false
                } catch (e: Exception) {
                    _error.value = "Erro ao carregar mais posts"
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Erro ao carregar mais posts: ${e.message}"
                _isLoading.value = false
            }
    }

    private fun processDocuments(documents: QuerySnapshot): List<Post> {
        return documents.mapNotNull { document ->
            try {
                val imageString = document.getString("image")
                val description = document.getString("description") ?: ""
                val email = document.getString("userEmail") ?: return@mapNotNull null
                val fullName = document.getString("fullName") ?: "Usuário"
                val profilePhotoString = document.getString("profilePhoto")
                val location = document.getString("location")
                val postBitmap = imageString?.let { Base64Converter.stringToBitmap(it) }
                val userProfilePhoto = if (!profilePhotoString.isNullOrEmpty()) {
                    Base64Converter.stringToBitmap(profilePhotoString)
                } else {
                    generateDefaultProfileBitmap()
                }

                Post(
                    description = description,
                    photo = postBitmap,
                    fullName = fullName,
                    userProfilePhoto = userProfilePhoto,
                    location = location,
                    userEmail = email
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun resetPagination() {
        lastDocument = null
        _isLastPage.value = false
        _posts.value = emptyList()
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    private fun generateDefaultProfileBitmap(): Bitmap {
        val bitmap = createBitmap(100, 100)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.LTGRAY)
        return bitmap
    }
}