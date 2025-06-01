package com.example.testbundle

import android.content.Context
import android.content.Intent
import com.example.testbundle.Activity.MainActivity
import com.example.testbundle.Repository.AuthRepository

// Создаём файл Extensions.kt
val Context.authRepository: AuthRepository
    get() = AuthRepository(this)

suspend fun Context.getAuthToken(): String? {
    return authRepository.getTokenWithRefresh()
}

suspend fun Context.withAuthToken(block: suspend (String) -> Unit) {
    getAuthToken()?.let { token ->
        block(token)
    } ?: run {
        // Токен не получен, перенаправляем на экран логина
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}