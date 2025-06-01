package com.example.testbundle.Repository

import android.content.Context
import android.util.Log
import com.example.testbundle.API.RetrofitClient
import com.google.gson.annotations.SerializedName
import retrofit2.Response

class AuthRepository(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val TAG = "AuthRepository"

    // Keys for shared preferences
    private companion object {
        const val ACCESS_TOKEN_KEY = "access_token"
        const val REFRESH_TOKEN_KEY = "refresh_token"
        const val TOKEN_EXPIRATION_KEY = "token_expiration"
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val loginResponse = response.body() ?: return Result.Error("Empty response body")

                if (loginResponse.token.isNullOrEmpty()) {
                    return Result.Error("Empty token received")
                }

                saveAuthData(
                    accessToken = loginResponse.token,
                    refreshToken = loginResponse.refreshToken ?: "",
                    expiresIn = loginResponse.expiresIn ?: 3600
                )

                Result.Success(loginResponse)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Invalid credentials"
                    403 -> "Access denied"
                    else -> "Login failed: ${response.code()}"
                }
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.Error(e.message ?: "Network error occurred")
        }
    }
    suspend fun getValidAccessToken(): Result<String> {
        val accessToken = getAccessToken()

        // Если токена нет вообще
        if (accessToken == null) {
            return Result.Error("No access token available")
        }

        // Если токен не истек, возвращаем его
        if (!isTokenExpired()) {
            return Result.Success(accessToken)
        }

        // Если токен истек, пытаемся обновить
        return when (val refreshResult = refreshToken()) {
            is Result.Success -> Result.Success(refreshResult.data)
            is Result.Error -> refreshResult
        }
    }

    suspend fun refreshToken(): Result<String> {
        val refreshToken = getRefreshToken() ?: return Result.Error("No refresh token available")

        return try {
            val response = RetrofitClient.apiService.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                val refreshResponse = response.body() ?: return Result.Error("Empty refresh response")

                if (refreshResponse.token.isNullOrEmpty()) {
                    return Result.Error("Empty token received")
                }

                saveAuthData(
                    accessToken = refreshResponse.token,
                    refreshToken = refreshResponse.refreshToken ?: refreshToken,
                    expiresIn = refreshResponse.expiresIn ?: 3600
                )

                Result.Success(refreshResponse.token)
            } else {
                Result.Error("Refresh failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Refresh token error", e)
            Result.Error(e.message ?: "Refresh token failed")
        }
    }

    fun getAccessToken(): String? {
        return sharedPrefs.getString(ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(): String? {
        return sharedPrefs.getString(REFRESH_TOKEN_KEY, null)
    }

    fun isTokenExpired(): Boolean {
        val expirationTime = sharedPrefs.getLong(TOKEN_EXPIRATION_KEY, 0)
        return System.currentTimeMillis() > expirationTime
    }

    fun logout() {
        sharedPrefs.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            remove(TOKEN_EXPIRATION_KEY)
            apply()
        }
    }

    private fun saveAuthData(accessToken: String, refreshToken: String, expiresIn: Int) {
        val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)

        sharedPrefs.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            putLong(TOKEN_EXPIRATION_KEY, expirationTime)
            apply()
        }
    }
    suspend fun getTokenWithRefresh(): String? {
        return when (val result = getValidAccessToken()) {
            is Result.Success -> "Bearer ${result.data}"
            is Result.Error -> {
                logout()
                null
            }
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    @SerializedName("token")
    val token: String,

    val refreshToken: String?,
    val expiresIn: Int?
)

data class RefreshTokenRequest(val refreshToken: String)
data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String?,
    val expiresIn: Int?
)