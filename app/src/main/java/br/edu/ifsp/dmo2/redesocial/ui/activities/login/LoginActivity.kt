package br.edu.ifsp.dmo2.redesocial.ui.activities.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityLoginBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.home.HomeActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListeners()
        setInputStyle()
    }

    private fun setOnClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Erro no login", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Preencha as informações", Toast.LENGTH_LONG).show()
            }
        }

        binding.arrowBack.setOnClickListener {
            finish()
        }
    }

    private fun setInputStyle() {
        InputColorUtils.applyInputColors(
            binding.inputEmailContainer,
            binding.inputPasswordContainer
        )
    }
}