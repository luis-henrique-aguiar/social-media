package br.edu.ifsp.dmo2.redesocial.ui.activities.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityProfileBinding
import br.edu.ifsp.dmo2.redesocial.ui.utils.InputColorUtils
import android.graphics.BitmapFactory
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import br.edu.ifsp.dmo2.redesocial.ui.activities.home.HomeActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var gallery: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInputStyle()
        openBundle()
        setupObservers()
        setupTextWatcher()
        setupGalleryPicker()
        setOnClickListener()
    }

    private fun setupGalleryPicker() {
        gallery = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.profileImage.setImageBitmap(bitmap)
                        viewModel.updateSelectedBitmap(bitmap)
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

    private fun openBundle() {
        val email = intent.extras?.getString("email")
        if (email != null) {
            viewModel.updateEmail(email)
        } else {
            Toast.makeText(this, "Erro: E-mail não fornecido", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.usernameError.observe(this) {
            binding.inputUsernameContainer.error = it
        }

        viewModel.fullNameError.observe(this) {
            binding.inputNameContainer.error = it
        }

        viewModel.registerSuccess.observe(this) {
            if (it) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }

        viewModel.isLoading.observe(this) {
            binding.saveDataButton.isEnabled = !it
        }
    }

    private fun setupTextWatcher() {
        binding.inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateFullName(s.toString())
                viewModel.clearErrors()
            }
        })

        binding.inputUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateUsername(s.toString())
                viewModel.clearErrors()
            }
        })
    }

    private fun setInputStyle() {
        InputColorUtils.applyInputColors(
            binding.inputNameContainer,
            binding.inputUsernameContainer
        )
    }

    private fun setOnClickListener() {
        binding.saveDataButton.setOnClickListener {
            viewModel.register()
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