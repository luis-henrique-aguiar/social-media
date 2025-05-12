package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo2.redesocial.R
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.redesocial.databinding.AddPostBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.edit.EditProfileActivity
import br.edu.ifsp.dmo2.redesocial.ui.activities.login.LoginActivity
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

        viewModel.resetPagination()

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
                            currentDialogBinding!!.addedImage.visibility = View.VISIBLE
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

        binding.feeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                val shouldLoadMore = !viewModel.isLoading.value!! &&
                        !viewModel.isLastPage.value!! &&
                        (visibleItemCount + firstVisibleItem) >= totalItemCount &&
                        totalItemCount >= 5

                if (shouldLoadMore) {
                    viewModel.loadMorePosts()
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.userData.observe(this) { userData ->
            userData.profilePhoto?.let { binding.profileImage.setImageBitmap(it) } ?: setDefaultProfileImage()
        }

        viewModel.posts.observe(this) { posts ->
            adapter.updatePosts(posts)
        }

        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.posts.observe(this) { posts ->
            if (viewModel.isLastPage.value == true && posts.size == adapter.itemCount) {
                return@observe
            }

            if (adapter.itemCount == 0 || posts.size <= adapter.itemCount) {
                adapter.clearAndAddPosts(posts)
            } else {
                adapter.updatePosts(posts.subList(adapter.itemCount, posts.size))
            }
        }

        viewModel.isLastPage.observe(this) { isLastPage ->
            if (isLastPage) {
                Toast.makeText(this, "Fim das postagens.", Toast.LENGTH_LONG).show()
            }
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
                val description = dialogBinding.inputDescription.text.toString()
                val image = drawable?.let { Base64Converter.drawableToString(drawable) }
                if (image != null || description.isNotBlank()) {
                    viewModel.addPost(image, description)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Selecione uma imagem ou coloque uma descrição.", Toast.LENGTH_LONG).show()
                }
            }

            dialogBinding.cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        binding.leaveIcon.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 300)
        }

        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 300)
        }
    }

    override fun onLocationReceived(address: Address, latitude: Double, longitude: Double) {
        runOnUiThread {
            viewModel.updateLocation(address.subAdminArea + ", " + address.adminArea)
        }
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "A localização é necessária para adicionar a cidade ao post.", Toast.LENGTH_LONG).show()
            }
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
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

    private fun setDefaultProfileImage() {
        val bitmap = Base64Converter.getDefaultBitmap()
        binding.profileImage.setImageBitmap(bitmap)
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
