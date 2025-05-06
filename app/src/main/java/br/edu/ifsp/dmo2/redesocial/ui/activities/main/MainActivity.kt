package br.edu.ifsp.dmo2.redesocial.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo2.redesocial.databinding.ActivityMainBinding
import br.edu.ifsp.dmo2.redesocial.ui.activities.home.HomeActivity
import br.edu.ifsp.dmo2.redesocial.ui.activities.login.LoginActivity
import br.edu.ifsp.dmo2.redesocial.ui.activities.register.RegisterActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListener()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isLogged.observe(this) {
            if (it) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }
    }

    private fun setOnClickListener() {
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}