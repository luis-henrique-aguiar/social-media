package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.redesocial.model.Post
import br.edu.ifsp.dmo2.redesocial.ui.activities.main.MainActivity
import br.edu.ifsp.dmo2.redesocial.ui.adapters.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var adapter: PostAdapter
    private lateinit var posts: ArrayList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListener()

        val db = Firebase.firestore
        val email = firebaseAuth.currentUser!!.email.toString()
        db.collection("users").document(email).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    val imageString = document.data?.get("profilePhoto")?.toString()
                    if (!imageString.isNullOrEmpty()) {
                        val bitmap = Base64Converter.stringToBitmap(imageString)
                        binding.profileImage.setImageBitmap(bitmap)
                    }
                    val username = document.data?.get("username")?.toString() ?: "Usuário"
                    binding.username.text = "Olá, $username!"
                    binding.email.text = email
                }
            }
    }

    private fun setOnClickListener() {
        binding.logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.feedButton.setOnClickListener {
            val db = Firebase.firestore
            db.collection("posts").get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentRef = task.result
                        posts = ArrayList<Post>()
                        for (document in documentRef.documents) {
                            val imageString = document.data!!["image"].toString()
                            val bitmap = Base64Converter.stringToBitmap(imageString)
                            val description = document.data!!["description"].toString()
                            posts.add(Post(description, bitmap!!))
                        }
                        adapter = PostAdapter(posts.toTypedArray())
                        binding.feeds.layoutManager = LinearLayoutManager(this)
                        binding.feeds.adapter = adapter
                    }
                }
        }
    }
}
