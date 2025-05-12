package br.edu.ifsp.dmo2.redesocial.model

import android.graphics.Bitmap

data class Post(
    val description: String,
    val photo: Bitmap?,
    val fullName: String,
    val userProfilePhoto: Bitmap?,
    val location: String?,
    val userEmail: String,
    val username: String
)