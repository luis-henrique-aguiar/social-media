package br.edu.ifsp.dmo2.redesocial.ui.activities.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityHomeBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.main.MainActivity
import br.edu.ifsp.dmo2.redesocial.ui.adapters.PostAdapter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: PostAdapter
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setOnClickListener()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(emptyArray())
        binding.feeds.layoutManager = LinearLayoutManager(this)
        binding.feeds.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.userData.observe(this) { userData ->
            userData.profilePhoto?.let { binding.profileImage.setImageBitmap(it) }
        }
    }

    private fun setOnClickListener() {

    }
}
