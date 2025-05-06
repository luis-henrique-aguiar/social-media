package br.edu.ifsp.dmo2.redesocial.ui.activities.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityLoginBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.home.HomeActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setOnClickListeners()
        setInputStyle()
    }

    private fun setupObservers() {
        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
        viewModel.success.observe(this) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        viewModel.isLoading.observe(this) {

        }
    }

    private fun setOnClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            viewModel.login(email, password)
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