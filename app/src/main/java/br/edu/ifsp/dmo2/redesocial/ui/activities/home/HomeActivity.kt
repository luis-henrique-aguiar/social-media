package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.redesocial.databinding.AddPostBinding
import br.edu.ifsp.dmo2.redesocial.ui.adapters.PostAdapter
import br.edu.ifsp.dmo2.redesocial.ui.utils.Base64Converter
import br.edu.ifsp.dmo2.redesocial.ui.utils.LocationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeActivity : AppCompatActivity(), LocationHelper.Callback {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: PostAdapter
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var gallery: ActivityResultLauncher<PickVisualMediaRequest>
    private var isDialogOpen = false
    private var currentDialogBinding: AddPostBinding? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

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

            requestLocation()

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

        binding.notificationIc.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onLocationReceived(address: Address, latitude: Double, longitude: Double) {
        runOnUiThread {
            viewModel.updateLocation(address.subAdminArea + ", " + address.adminArea)
        }
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val locationHelper = LocationHelper(applicationContext)
            locationHelper.getCurrentLocation(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(message: String) {

    }
}
