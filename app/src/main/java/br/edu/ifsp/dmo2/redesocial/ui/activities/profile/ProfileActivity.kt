package br.edu.ifsp.dmo2.redesocial.ui.activities.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityProfileBinding
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var selectedBitmap: Bitmap? = null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var gallery: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setInputStyle()
        setupGalleryPicker()
        setOnClickListener()
    }

    private fun openBundle(): Pair<String, String>? {
        val extras = intent.extras
        val email = extras?.getString("email")
        val password = extras?.getString("password")
        return if (email != null && password != null) Pair(email, password) else null
    }

    private fun setupGalleryPicker() {
        gallery = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.profileImage.setImageBitmap(bitmap)
                        selectedBitmap = bitmap
                    } ?: run {
                        Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar imagem: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createUser() {
        checkUsernameAvailability(username) { isAvailable ->
            if (isAvailable) {
                firebaseAuth
                    .createUserWithEmailAndPassword(credentials.first, credentials.second)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (firebaseAuth.currentUser != null) {
                                val email = firebaseAuth.currentUser!!.email.toString()
                                val data = getUserData()
                                saveUserData(email, username, data)
                            }
                        } else {
                            val errorMessage = when (task.exception) {
                                is FirebaseAuthUserCollisionException -> "E-mail já está em uso."
                                is FirebaseAuthWeakPasswordException -> "A senha é muito fraca."
                                is FirebaseAuthInvalidCredentialsException -> "Credenciais inválidas."
                                else -> "Erro ao criar usuário: ${task.exception?.message}"
                            }
                            val resultIntent = Intent().putExtra("error_message", errorMessage)
                            setResult(RESULT_CANCELED, resultIntent)
                            finish()
                        }
                    }
            } else {
                binding.inputUsernameContainer.error = "Username não disponível"
                Toast.makeText(this, "Username não disponível", Toast.LENGTH_LONG).show();
            }
        }
    }

    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        val db = Firebase.firestore
        db.collection("usernames").document(username).get()
            .addOnSuccessListener { document ->
                callback(!document.exists())
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao verificiar username: ${e.message}", Toast.LENGTH_LONG).show()
                callback(false)
            }
    }

    private fun getUserData(): HashMap<String, String> {
        val username = binding.inputUsername.text.toString()
        val fullName = binding.inputName.text.toString()
        val profilePhotoString = convertImageToBase64()
        return hashMapOf(
            "fullName" to fullName,
            "username" to username,
            "profilePhoto" to profilePhotoString
        )
    }

    private fun convertImageToBase64(): String {
        return selectedBitmap?.let { bitmap ->
            Base64Converter.bitmapToString(bitmap)
        } ?: binding.profileImage.drawable?.let { drawable ->
            Base64Converter.drawableToString(drawable)
        } ?: ""
    }

    private fun saveUserData(email: String, username: String, data: HashMap<String, String>) {
        val db = Firebase.firestore
        db.runTransaction { transaction ->
            val usernameRef = db.collection("usernames").document(username)
            val userRef = db.collection("users").document(email)
            if (transaction.get(usernameRef).exists()) throw FirebaseFirestoreException("Username ja está em uso.", FirebaseFirestoreException.Code.ABORTED)
            transaction.set(userRef, data)
            transaction.set(usernameRef, hashMapOf("email" to email))
        }.addOnSuccessListener {
            setResult(RESULT_OK)
            finish()
        }.addOnFailureListener { e ->
            val resultIntent = Intent().putExtra("error_message", "Erro ao salvar dados: ${e.message}")
            setResult(RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    private fun setInputStyle() {
        InputColorUtils.applyInputColors(
            binding.inputNameContainer,
            binding.inputUsernameContainer
        )
    }

    private fun setOnClickListener() {
        binding.saveDataButton.setOnClickListener {
            createUser()
        }

        binding.changeImageButton.setOnClickListener {
            gallery.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.arrowBack.setOnClickListener {
            finish()
        }
    }
}