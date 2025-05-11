package br.edu.ifsp.dmo2.redesocial.ui.activities.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityRegisterBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.profile.ProfileActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils
import com.google.android.material.textfield.TextInputEditText

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
        setupTextWatcher(binding.inputConfirmPassword) { viewModel.updateConfirmPassword(it) }
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
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("email", viewModel.email.value)
                startActivity(intent)
            }
        }

        viewModel.isLoading.observe(this) {
            binding.registerButton.isEnabled = !it
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) {
            if (it != null) {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
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