package com.example.testbundle.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.User.ProfileActivity
import com.example.testbundle.Activity.User.ProfileActivity.Companion.language
import com.example.testbundle.LocaleUtils
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityMainBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class MainActivity : BaseActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding null")

    lateinit var prefs : DataStore<Preferences>
    companion object {
        val INITIALIZED_KEY = booleanPreferencesKey("is_initialized")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = MainDb.getDb(this)
        super.onCreate(savedInstanceState)
        val savedLanguage = LocaleUtils.getSavedLanguage(this)
        LocaleUtils.setLocale(this, savedLanguage)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = applicationContext.dataStore

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
        binding.arrowLeave.setOnClickListener {
            exitApp(this)
        }

        lifecycleScope.launch {
            val isInitialized = prefs.data.first()[INITIALIZED_KEY] ?: false
            if (!isInitialized) {
                init(MainDb.getDb(this@MainActivity))
            }
        }
        /**
         * Вход в аккаунт
         */
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim()
            val password = binding.etPassword.text?.toString()?.trim()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(this@MainActivity,
                    getString(R.string.email_and_password_cannot_be_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.getDao().getAllItems().asLiveData().observe(this) { userList ->
                val user = userList.find { it.email == email && it.password == password }
                if (user == null) {
                    Toast.makeText(this@MainActivity,
                        getString(R.string.incorrect_email_or_password), Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        prefs.edit {
                            it[ProfileActivity.EMAIL_KEY] = email
                            it[ProfileActivity.PASSWORD_KEY] = password
                            it[DataStoreRepo.USER_ID_KEY] = user.id ?: -1
                        }
                    }
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        binding.btnLanguage.setOnClickListener {
            changeLanguage()
        }
    }

    /**
     * Инициализация данных для входа администратора
     */
    suspend fun init(db: MainDb) {

        val isInitialized = prefs.data.first()[INITIALIZED_KEY] ?: false
        if (isInitialized) return

        val item = Item(
            null,
            "Passwords",
            "Mark",
            "Vesenkov",
            "markVesna@gmail.com",
            "+79990008912",
            getString(R.string.Administarator)
        )

        db.getDao().insertItem(item)


        prefs.edit { preferences ->
            preferences[INITIALIZED_KEY] = true
        }
    }

    /**
     * Выход из приложения
     */
    fun exitApp(context: Context) {
        (context as Activity).finishAffinity()
        System.exit(0)
    }

    private fun changeLanguage() {
        val languageCode = if (!language) "ru" else "en"
        LocaleUtils.setLocale(this, languageCode)
        language = !language
        recreate()
    }

}
