package com.example.testbundle.Activity.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.MainActivity
import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityCreateUserBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb

class CreateUserActivity : BaseActivity() {
    lateinit var binding: ActivityCreateUserBinding
    val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = MainDb.getDb(this)
        /**
         *  Кнопка создания пользователя
         */
        binding.btnSignIn.setOnClickListener {
            val email = binding.etLogin.text?.toString() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val name = binding.etName.text.toString()
            val surname = binding.etSurName.text.toString()
            val telephone = binding.etTelephone.text.toString()
            val isValid = validatePassword(password)
            val isNumber = "[0123456789]".toRegex()

            /**
            *Валидация
             * email [String] password [String]
            */
            if (email.isEmpty()) {
                validaciaIsEmpty(binding.etLogin)
                return@setOnClickListener
            }
            if (!email.contains("@mail.ru") &&
                !email.contains("@gmail.com") &&
                !email.contains("@yandex.ru")) {
                binding.etLogin.error = getString(R.string.please_input_correct_mail)
                return@setOnClickListener
            }
            if (email.length > 254 || email.length < 8) {
                binding.etLogin.error = getString(R.string.please_input_correct_mail)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                validaciaIsEmpty(binding.etPassword)
                return@setOnClickListener
            }
            if (!isValid) {
                return@setOnClickListener
            }
            if (name.trim().isEmpty()) {
                validaciaIsEmpty(binding.etName)
                return@setOnClickListener
            }
            if (name.length <=1) {
                binding.etName.error=getString(R.string.please_insert_correct_name)
                return@setOnClickListener
            }
            if (surname.trim().isEmpty()) {
                validaciaIsEmpty(binding.etSurName)
                return@setOnClickListener
            }
            if (telephone.trim().isEmpty()) {
                validaciaIsEmpty(binding.etTelephone)
                return@setOnClickListener
            }
            if (!telephone.startsWith("+")) {
                binding.etTelephone.error = getString(R.string.please_start_with)
                return@setOnClickListener
            }
            if (!telephone.contains(isNumber)) {
                binding.etTelephone.error = getString(R.string.please_only_numbers)
                return@setOnClickListener
            }
            if (telephone.length < 6 || telephone.length > 15) {
                binding.etTelephone.error = getString(R.string.telephone_must_be_6_to_15_character)
                return@setOnClickListener
            }

            /**
             * Проверка на использовании почтой другим пользователями
             */
            db.getDao().getAllItems().asLiveData().observe(this) { list ->
                if (list?.any { it.email == email } == true) {
                    binding.etLogin.error = getString(R.string.this_email_is_used)
                } else {
                    val item = Item(
                        null,
                        password,
                        name,
                        surname,
                        email,
                        telephone,
                        binding.SpinnerSpecialitety.selectedItem.toString(),
                        null
                    )
                    viewModel.insertItem(item)
                    Toast.makeText(
                        this@CreateUserActivity,
                        getString(R.string.account_success_create),
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@CreateUserActivity, ListEmployeeActivity::class.java))
                    finish()
                }
            }
        }

        /**
         * Кнопка назад
         */
        binding.btnArrowBack.setOnClickListener {
            val intent = Intent(this@CreateUserActivity, ListEmployeeActivity::class.java)
            startActivity(intent)
        }



    }
    fun validaciaIsEmpty(et: EditText) {
        et.error = getString(R.string.nullable)
    }

    /**
     * Валидация пароля
     * @param password[String]
     */
    fun validatePassword(password: String?): Boolean {
        with(binding) {
            if (password.isNullOrEmpty()) {
                etPassword.error = getString(R.string.password_cannot_be_empty)
                return false
            }
            if (password.length < 8 || password.length > 15) {
                etPassword.error = getString(R.string.password_must_be_8_to_15_characters_long)
                return false
            }
            val passwordPattern = Regex(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+\$"
            )

            if (!passwordPattern.matches(password)) {
                etPassword.error = getString(R.string.password_must_contain_at_least) +
                        getString(R.string._1_uppercase_letter_a_z) +
                        getString(R.string._1_lowercase_letter_a_z) +
                        getString(R.string._1_digit_0_9) +
                        getString(R.string._1_special_character_etc)
                return false
            }
            etPassword.error = null
            return true
        }
    }
}



