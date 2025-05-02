package com.example.testbundle.Activity.User

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Activity.User.ListProductActivity.Companion.idUser
import com.example.testbundle.Activity.User.ProfileActivity.Companion.idAccount
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityUpdateInformationBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random

class UpdateInformationActivity : BaseActivity() {
    lateinit var binding: ActivityUpdateInformationBinding
    private var check:Boolean? = null
    private var emailMap = mutableMapOf<String,Boolean>()
    val viewModel: MainViewModel by viewModels<MainViewModel>()
    private var randomValues:String?=null
    lateinit var prefs: DataStore<androidx.datastore.preferences.core.Preferences>
    private var currentUserId:Int?=-1

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUpdateInformationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val db = MainDb.getDb(this)
        prefs = applicationContext.dataStore
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect {
                    currentUserId = it[DataStoreRepo.USER_ID_KEY]
                }
            }
        }
        /**
         * Выводит информацию пользователя
         */
        lifecycleScope.launch {
            val account = db.getDao().getAccountById(idAccount)
            binding.etName.setText(account.Name)
            binding.etLogin.setText(account.email)
            binding.etSurName.setText(account.SurName)
            binding.etPassword.setText(account.password)
            binding.etTelephone.setText(account.telephone)
            binding.tvSpecialitetyVivod.setText(account.speciality)
            emailMap.put(account.email,true)
        }

        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this@UpdateInformationActivity, ProfileActivity::class.java))
        }
        /**
         * Кнопка обновления информации пользователя
         */
        binding.btnUpdate.setOnClickListener {
            val email = binding.etLogin.text?.toString()

            val name = binding.etName.text.toString()
            val telephone = binding.etTelephone.text.toString()
            check = emailMap.get(binding.etLogin.text.toString())
            if (!check!!){
                binding.etLogin.error="erroreeeee"
                return@setOnClickListener
            }
            var validaciaTrue = false
            db.getDao().getAllItems().asLiveData().observe(this) { list ->
                var isEmailUsedByOthers = false
                list.forEach { user ->
                    with(binding) {
                        if (email.isNullOrEmpty()) {
                            validaciaIsEmpty(etLogin)
                        } else if (!email.contains("@mail.ru") && !email.contains("@gmail.com") && !email.contains("@yandex.ru")) {
                            etLogin.error = getString(R.string.please_input_correct_mail)
                            validaciaTrue = false
                        } else if (user.email == email && user.id != currentUserId) {
                            etLogin.error =
                                getString(R.string.this_email_is_used_by_another_account)
                            isEmailUsedByOthers = true
                            validaciaTrue = false
                        }
                        else if(email.length>254 || email.length<8){
                            etLogin.error = getString(R.string.please_input_correct_mail)
                            validaciaTrue = false
                        }else if (name.trim().isEmpty()) {
                            validaciaIsEmpty(etName)
                        }  else if(name.length<=1){
                            etName.error = getString(R.string.please_insert_correct_name)
                            validaciaTrue = false
                        }else if (telephone.trim().isEmpty()) {
                            validaciaIsEmpty(etTelephone)
                        } else if (!telephone.startsWith("+")) {
                            etTelephone.error = getString(R.string.please_start_with)
                        } else if (telephone.length < 6 || telephone.length > 15) {
                            etTelephone.error = getString(R.string.telephone_must_be_6_to_15_character)
                            validaciaTrue = false
                        } else {
                            validaciaTrue = true
                        }
                    }
                }


                if (validaciaTrue && !isEmailUsedByOthers) {
                    val pass = binding.etPassword.text.toString()
                    val name = binding.etName.text.toString()
                    val surname = binding.etSurName.text.toString()
                    val email = binding.etLogin.text.toString()
                    val telephone = binding.etTelephone.text.toString()
                    val speciality = binding.tvSpecialitetyVivod.text.toString()
                    val updatedUser = Item(idAccount, pass, name, surname, email, telephone, speciality)
                    lifecycleScope.launch {
                        viewModel.updateItem(updatedUser)
                        prefs.edit { preferences ->
                            preferences[ProfileActivity.EMAIL_KEY] = email
                            preferences[ProfileActivity.PASSWORD_KEY] = pass
                            preferences[DataStoreRepo.USER_ID_KEY] = idAccount
                        }
                        val intent = Intent(this@UpdateInformationActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    }

                }
            }
        }
        binding.newPassword.setOnClickListener {
            showChangePasswordDialog()
        }
        binding.btnConfirmEmail.setOnClickListener {
            val email = binding.etLogin.text.toString()
            check = emailMap.get(binding.etLogin.text.toString())
            if (check==null) check=false
            if (check!!) {
                Toast.makeText(this@UpdateInformationActivity,getString(R.string.succesCheckEmail),Toast.LENGTH_SHORT).show()
            } else {
                emailMap.put(email,false)
                Thread(Runnable {
                    sendEmail(email)
                }).start()
                showConfirmEmailDialog()
            }
        }

    }

    /**
     * Диалоговое окно со смена пароля
     */
    private fun showChangePasswordDialog() {
        val dialog = AlertDialog.Builder(this@UpdateInformationActivity)
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnLeave = dialogView.findViewById<ImageButton>(R.id.btnLeave)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        val customdialog = dialog.create()

        lifecycleScope.launch {
            val db = MainDb.getDb(this@UpdateInformationActivity)
            val currentUser = db.getDao().getAccountById(currentUserId ?: -1)

            btnConfirm.setOnClickListener {
                val oldPassword = etOldPassword.text?.toString() ?: ""
                val newPassword = etNewPassword.text?.toString() ?: ""
                val confirmPassword = etConfirmPassword.text?.toString() ?: ""
                when {
                    oldPassword != currentUser.password -> {
                        Toast.makeText(this@UpdateInformationActivity,
                            getString(R.string.password_dont_with_old_password), Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(this@UpdateInformationActivity,
                            getString(R.string.check_new_password), Toast.LENGTH_SHORT).show()
                    }
                    !validatePassword(newPassword) -> {
                        Toast.makeText(this@UpdateInformationActivity,
                            getString(R.string.new_password_dont_success_tz), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val updatedUser = currentUser.copy(password = confirmPassword)
                        lifecycleScope.launch {
                            viewModel.updateItem(updatedUser)
                            prefs.edit { preferences ->
                                preferences[ProfileActivity.PASSWORD_KEY] = confirmPassword
                            }
                            binding.etPassword.setText(confirmPassword)
                            customdialog.dismiss()
                            Toast.makeText(this@UpdateInformationActivity,
                                getString(R.string.password_success_edit), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@UpdateInformationActivity, ProfileActivity::class.java)
                            startActivity(intent)
                        }

                    }
                }
            }
        }

        btnLeave.setOnClickListener {
            customdialog.dismiss()
        }
        customdialog.show()
    }
    fun validaciaIsEmpty(et: EditText) {
        et.error = getString(R.string.nullable)
    }

    /**
     * Функция валидация пароля
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
    fun sendEmail(toEmail: String) {
        try {
            val props = Properties()
            props.setProperty("mail.transport.protocol", "smtp")
            props.setProperty("mail.host", "smtp.gmail.com")
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")

            randomValues = Random.nextInt(1000, 9999).toString()


            val session = Session.getDefaultInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    //от кого
                    return PasswordAuthentication("isip_m.a.vesenkov@mpt.ru", "mbmtqsqhtxxwurzn")
                }
            })
            val message = MimeMessage(session)//от кого
            message.setFrom(InternetAddress("isip_m.a.vesenkov@mpt.ru"))//куда
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
            message.subject = "This is code"
            message.setText(randomValues)

            val transport = session.getTransport("smtp")
            transport.connect()
            Transport.send(message)
            transport.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //то что я хочу
    }
    private fun showConfirmEmailDialog() {
        val dialog = AlertDialog.Builder(this@UpdateInformationActivity)
        val dialogView = layoutInflater.inflate(R.layout.checkemaildialog, null)
        val etCode = dialogView.findViewById<EditText>(R.id.etCode)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnLeave = dialogView.findViewById<ImageButton>(R.id.btnLeave)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        val customdialog = dialog.create()

        btnConfirm.setOnClickListener {
            val code = etCode.text?.toString() ?: ""
            if (code==randomValues){
                binding.btnConfirmEmail.setBackgroundResource(R.color.green)
                emailMap.set(binding.etLogin.text.toString(),true)

                customdialog.dismiss()
                Toast.makeText(this@UpdateInformationActivity,getString(R.string.succesCheckEmail),Toast.LENGTH_SHORT).show()
            }else{
                binding.btnConfirmEmail.setBackgroundResource(R.color.red)
                emailMap.set(binding.etLogin.text.toString(),false)

                Toast.makeText(this@UpdateInformationActivity,getString(R.string.unsuccessfullyCheckEmail),Toast.LENGTH_SHORT).show()
            }

        }
        btnLeave.setOnClickListener {
            customdialog.dismiss()
        }
        customdialog.show()
    }


}





