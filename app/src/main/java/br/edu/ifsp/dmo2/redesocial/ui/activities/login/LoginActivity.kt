package br.edu.ifsp.dmo2.redesocial.ui.activities.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
        setupTextWatchers()
    }

    private fun setupObservers() {
        viewModel.emailError.observe(this) { error ->
            binding.inputEmailContainer.error = error
        }

        viewModel.passwordError.observe(this) { error ->
            binding.inputPasswordContainer.error = error
        }

        viewModel.success.observe(this) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.loginButton.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setOnClickListeners() {
        binding.loginButton.setOnClickListener {
            viewModel.login()
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

    private fun setupTextWatchers() {
        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateEmail(s.toString())
            }
        })

        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updatePassword(s.toString())
            }
        })

        binding.inputEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearErrors()
        }
        binding.inputPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearErrors()
        }
    }
}