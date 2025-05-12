package br.edu.ifsp.dmo2.redesocial.ui.activities.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityLoginBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.home.HomeActivity
import br.edu.ifsp.dmo2.redesocial.ui.activities.main.MainActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils
import com.google.android.material.textfield.TextInputEditText

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
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 300)
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
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 300)
        }
    }

    private fun setInputStyle() {
        InputColorUtils.applyInputColors(
            binding.inputEmailContainer,
            binding.inputPasswordContainer
        )
    }

    private fun setupTextWatcher(input: TextInputEditText, updateFunc: (String) -> Unit) {
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateFunc(s.toString())
                viewModel.clearErrors()
            }
        })
    }

    private fun setupTextWatchers() {
        setupTextWatcher(binding.inputEmail) { viewModel.updateEmail(it) }
        setupTextWatcher(binding.inputPassword) { viewModel.updatePassword(it) }
    }
}