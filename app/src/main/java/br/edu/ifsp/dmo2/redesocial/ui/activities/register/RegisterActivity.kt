package br.edu.ifsp.dmo2.redesocial.ui.activities.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.ui.activities.profile.ProfileActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityRegisterBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.home.HomeActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater);
        setContentView(binding.root)

        setInputStyle()
        setResultLauncher()
        setOnClickListener()
    }

    private fun setResultLauncher() {
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
                if (result.resultCode == RESULT_OK) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    val errorMessage = result.data?.getStringExtra("error_message") ?: "Erro desconhecido."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun redirect(email: String, password: String) {
        val mIntent = Intent(this, ProfileActivity::class.java)
        mIntent.putExtra("email", email)
        mIntent.putExtra("password", password);
        resultLauncher.launch(mIntent)
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
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val confirmPassword = binding.inputConfirmPassword.text.toString()
            when {
                email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                    Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_LONG).show()
                }
                !isValidEmail(email) -> {
                    binding.inputEmailContainer.error = "E-mail inválido."
                }
                !isValidPassword(password) -> {
                    binding.inputPasswordContainer.error = "A senha deve ter pelo menos 6 dígitos, 1 letra e 1 número."
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "As senhas não são iguais", Toast.LENGTH_LONG).show()
                }
                else -> redirect(email, password)
            }
        }

        binding.arrowBack.setOnClickListener {
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6 && password.any {  it.isDigit() } && password.any { it.isLetter() }
    }
}