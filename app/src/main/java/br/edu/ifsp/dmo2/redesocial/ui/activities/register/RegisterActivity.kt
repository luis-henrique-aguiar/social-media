package br.edu.ifsp.dmo2.redesocial.ui.activities.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityRegisterBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.profile.ProfileActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater);
        setContentView(binding.root)

        setInputStyle()
        setupObservers()
        setupTextWatchers()
        setOnClickListener()
    }

    private fun setupTextWatchers() {
        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateEmail(s.toString())
                viewModel.clearErrors()
            }
        })

        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updatePassword(s.toString())
                viewModel.clearErrors()
            }
        })

        binding.inputConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateConfirmPassword(s.toString())
                viewModel.clearErrors()
            }
        })
    }

    private fun setupObservers() {
        viewModel.emailError.observe(this) {
            binding.inputEmailContainer.error = it
        }

        viewModel.passwordError.observe(this) {
            binding.inputPasswordContainer.error = it
        }

        viewModel.confirmPasswordError.observe(this) {
            binding.inputConfirmPasswordContainer.error = it
        }

        viewModel.registerSuccess.observe(this) {
            if (it) {
                val mIntent = Intent(this, ProfileActivity::class.java)
                mIntent.putExtra("email", viewModel.email.value)
                startActivity(mIntent)
            }
        }

        viewModel.isLoading.observe(this) {
            binding.registerButton.isEnabled = !it
        }
    }

    private fun setInputStyle() {
        InputColorUtils.applyInputColors(
            binding.inputEmailContainer,
            binding.inputPasswordContainer,
            binding.inputConfirmPasswordContainer
        )
    }

    private fun setOnClickListener() {
        binding.registerButton.setOnClickListener {
            viewModel.register()
        }

        binding.arrowBack.setOnClickListener {
            finish()
        }
    }
}