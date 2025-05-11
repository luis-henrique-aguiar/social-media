package br.edu.ifsp.dmo2.redesocial.ui.utils

import android.util.Patterns

object Validator {
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Preencha o campo de e-mail"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "E-mail inválido"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Preencha a senha"
            password.length < 6 -> "A senha deve ter pelo menos 6 caracteres"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Preencha o campo de confirmação de senha"
            confirmPassword != password -> "As senhas não são iguais"
            else -> null
        }
    }

    fun validateFullName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "Preencha o campo de nome"
            !Regex("^[\\p{L}\\s-]+$").matches(fullName) -> "O nome deve conter apenas letras, espaços ou hífens"
            else -> null
        }
    }

    fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Preencha o campo de username"
            !Regex("^[A-Za-z0-9_]+$").matches(username) -> "O nome de usuário deve conter apenas letras, números e sublinados"
            else -> null
        }
    }
}