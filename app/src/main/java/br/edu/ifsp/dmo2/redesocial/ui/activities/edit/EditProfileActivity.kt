package br.edu.ifsp.dmo2.redesocial.ui.activities.edit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityEditProfileBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.home.HomeActivity
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import java.io.ByteArrayOutputStream
import java.util.Base64

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private lateinit var gallery: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnClickListeners()
        setupGalleryPicker()
        setupObservers()
        setupTextWatcher()
    }

    private fun setupGalleryPicker() {
        gallery = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.profileImage.setImageBitmap(bitmap)
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                        val base64String = Base64.getEncoder().encodeToString(baos.toByteArray())
                        viewModel.updateProfilePicture(base64String)
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

    private fun setupTextWatcher() {
        binding.inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateFullName(s.toString())
            }
        })

        binding.inputCurrentPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateCurrentPassword(s.toString())
            }
        })

        binding.inputNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateNewPassword(s.toString())
            }
        })
    }

    private fun setupObservers() {
        viewModel.fullNameError.observe(this) {
            binding.inputNameContainer.error = it
        }

        viewModel.currentPasswordError.observe(this) {
            binding.inputCurrentPasswordContainer.error = it
        }

        viewModel.newPasswordError.observe(this) {
            binding.inputNewPasswordContainer.error = it
        }

        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.dataLoaded.observe(this) {
            if (it) {
                binding.inputName.setText(viewModel.fullName.value)
                val profileImage = viewModel.profilePicture.value
                if (profileImage != null) {
                    try {
                        binding.profileImage.setImageBitmap(Base64Converter.stringToBitmap(profileImage))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Erro ao carregar imagem do perfil: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.profileImage.setOnClickListener {
            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.editIcon.setOnClickListener {
            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.editButton.setOnClickListener {
            viewModel.updateUser()
        }

        binding.arrowBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}