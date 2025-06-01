package com.example.testbundle.Activity.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient

import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityEditAccountCardBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditAccountCardActivity : BaseActivity() {


    lateinit var binding: ActivityEditAccountCardBinding
    val viewModel : MainViewModel by viewModels<MainViewModel>()
     var avatar: String?=""
    private val productApi = RetrofitClient.apiService
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityEditAccountCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("item_id",0)

        /**
        Вывод информации пользователя
         */
        lifecycleScope.launch {
            withAuthToken {  token->
            val account = productApi.getUsersByID(id,token)
            account?.let {
                withContext(Dispatchers.Main) {
                    binding.etName.setText(it.name)
                    binding.etLogin.setText(it.email)
                    binding.etSurName.setText(it.surname)
                    binding.etPassword.setText(it.password)
                    binding.etTelephone.setText(it.telephone)
                    avatar = it.avatar
                    val specialityArray = resources.getStringArray(R.array.speciality)
                    val position = specialityArray.indexOf(it.speciality)
                    if (position >= 0) {
                        binding.SpinnerSpecialitety.setSelection(position)
                    }
                }
                }
            }
        }

        /**
         * Кнопка обновления информации
         */
        binding.btnUpdate.setOnClickListener {
            val email = binding.etLogin.text?.toString() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val name = binding.etName.text.toString()
            val surname = binding.etSurName.text.toString()
            val telephone = binding.etTelephone.text.toString()
            val specifer = binding.SpinnerSpecialitety.selectedItem.toString()

            val isValid = validatePassword(password)
            val isNumber = "[0123456789]".toRegex()

            /**
             * Проверка на валидацию
             */
            if (email.isEmpty()) {
                validaciaIsEmpty(binding.etLogin)
                return@setOnClickListener
            }
            if (!email.contains("@mail.ru") &&
                !email.contains("@gmail.com") &&
                !email.contains("@yandex.ru")
            ) {
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
            if (name.length <= 1) {
                binding.etName.error = getString(R.string.please_insert_correct_name)
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
            lifecycleScope.launch {
                try {
                    val emailExists = withContext(Dispatchers.IO) {
                        viewModel.checkEmailExists(email)
                    }

                    if (emailExists) {
                        binding.etLogin.error = getString(R.string.this_email_is_used)
                    } else {
                        withContext(Dispatchers.IO) {
                            withAuthToken { token->
                            val account = productApi.getUsersByID(id, token)
                            val user = Item(id,password,name,surname,email,telephone,binding.SpinnerSpecialitety.selectedItem.toString(),account.avatar)
                            viewModel.updateItem(
                                id,
                               user
                            )
                        }}

                        Toast.makeText(
                            this@EditAccountCardActivity,
                            getString(R.string.account_success_create),
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this@EditAccountCardActivity,
                                ListEmployeeActivity::class.java
                            )
                        )
                        finish()
                    }
                } catch (e: Exception) {
                    // Обработка ошибок
                    Toast.makeText(
                        this@EditAccountCardActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }










        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this@EditAccountCardActivity,ListEmployeeActivity::class.java))
        }
    }
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
    fun validaciaIsEmpty(et: EditText) {
        et.error = "null"
    }

}

