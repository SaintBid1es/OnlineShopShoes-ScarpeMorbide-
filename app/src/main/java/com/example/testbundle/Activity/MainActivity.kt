package com.example.testbundle.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient


import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.User.ForgenPasswordActivity
import com.example.testbundle.Activity.User.ProfileActivity
import com.example.testbundle.LocaleUtils
import com.example.testbundle.R
import com.example.testbundle.Repository.AuthRepository
import com.example.testbundle.databinding.ActivityMainBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random


class MainActivity : BaseActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding null")

    lateinit var prefs: DataStore<Preferences>
    private lateinit var authRepository: AuthRepository
    companion object {
        val INITIALIZED_KEY = booleanPreferencesKey("is_initialized")
    }
    private val productApi = RetrofitClient.apiService
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        val savedLanguage = LocaleUtils.getSavedLanguage(this)
        LocaleUtils.setLocale(this, savedLanguage)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = applicationContext.dataStore

        authRepository = AuthRepository(applicationContext)


        /**
         * Переход на окно с регистрацией
         */
        binding.tvSignUp.setOnClickListener {
            intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        /**
         * Выход из приложения
         */
//        binding.arrowLeave.setOnClickListener {
//            exitApp(this)
//        }

        lifecycleScope.launch {
            val isInitialized = prefs.data.first()[INITIALIZED_KEY] ?: false
            if (!isInitialized) {
                init()
            }
        }
        /**
         * Вход в аккаунт
         */
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim()
            val password = binding.etPassword.text?.toString()?.trim()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.email_and_password_cannot_be_empty), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {

                    try {
                        when (val result = authRepository.login(email, password)) {

                            is com.example.testbundle.Repository.Result.Error -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    result.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is com.example.testbundle.Repository.Result.Success<*> -> {
                                withAuthToken { token ->
                                    val userList = productApi.getUsers(token)
                                    val user = userList.find { it.email == email && it.password == password }
                                lifecycleScope.launch {
                                    prefs.edit {
                                        it[ProfileActivity.EMAIL_KEY] = email
                                        it[ProfileActivity.PASSWORD_KEY] = password
                                        it[DataStoreRepo.USER_ID_KEY] = user!!.id ?: -1
                                    }
                                }
                                }
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        ProfileActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "Network error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }


//        binding.btnLanguage.setOnClickListener {
//            changeLanguage()
//        }
        binding.forgotPasswordLink.setOnClickListener {
            startActivity(Intent(this@MainActivity, ForgenPasswordActivity::class.java))
        }
    }




    /**
     * Инициализация данных для входа администратора
     */
    suspend fun init() {

        val isInitialized = prefs.data.first()[INITIALIZED_KEY] ?: false
        if (isInitialized) return

        val item = Item(
            null,
            "Passwords",
            "Mark",
            "Vesenkov",
            "markVesna@gmail.com",
            "+79990008912",
            getString(R.string.Administarator),
            null
        )

            productApi.insertUser(item)

        prefs.edit { preferences ->
            preferences[INITIALIZED_KEY] = true
        }
    }






}
