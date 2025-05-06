package br.edu.ifsp.dmo2.redesocial.ui.activities.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _isLogged = MutableLiveData<Boolean>()
    val isLogged: LiveData<Boolean> get() = _isLogged

    init {
        isUserLoggedIn()
    }

    private fun isUserLoggedIn() {
        _isLogged.value = firebaseAuth.currentUser != null
    }
}