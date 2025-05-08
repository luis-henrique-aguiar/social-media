package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.redesocial.databinding.AddPostBinding
import br.edu.ifsp.dmo2.redesocial.ui.adapters.PostAdapter
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: PostAdapter
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var gallery: ActivityResultLauncher<PickVisualMediaRequest>
    private var isDialogOpen = false
    private var currentDialogBinding: AddPostBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loadPosts()

        setupGalleryPicker()
        setupRecyclerView()
        setupObservers()
        setOnClickListener()
    }

    private fun setupGalleryPicker() {
        gallery = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val targetImageView = if (isDialogOpen && currentDialogBinding != null) {
                            currentDialogBinding!!.addedImage
                        } else {
                            binding.profileImage
                        }
                        targetImageView.setImageBitmap(bitmap)
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

    private fun setupRecyclerView() {
        adapter = PostAdapter()
        binding.feeds.layoutManager = LinearLayoutManager(this)
        binding.feeds.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.userData.observe(this) { userData ->
            userData.profilePhoto?.let { binding.profileImage.setImageBitmap(it) }
        }

        viewModel.posts.observe(this) {
            adapter.updatePosts(it)
        }

        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) {
            it?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }
    }

    private fun setOnClickListener() {
        binding.addPostButton.setOnClickListener {
            val dialogBinding = AddPostBinding.inflate(layoutInflater)

            isDialogOpen = true
            currentDialogBinding = dialogBinding

            val dialog = MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
                .setView(dialogBinding.root)
                .setOnDismissListener {
                    isDialogOpen = false
                    currentDialogBinding = null
                }
                .show()

            dialogBinding.addImage.setOnClickListener {
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            dialogBinding.confirmButton.setOnClickListener {
                val drawable = dialogBinding.addedImage.drawable
                if (drawable != null) {
                    val image = Base64Converter.drawableToString(drawable)
                    val description = dialogBinding.inputDescription.text.toString()
                    if (description.isNotBlank()) {
                        viewModel.addPost(image, description)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Descrição não pode estar vazia", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_SHORT).show()
                }
            }

            dialogBinding.cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }
    }
}
