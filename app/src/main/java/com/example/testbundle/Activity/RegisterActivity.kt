package com.example.testbundle.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.Repository.AuthRepository
import com.example.testbundle.databinding.ActivityRegisterBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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


class RegisterActivity : BaseActivity() {
    private var emailMap = mutableMapOf<String,Boolean>()
    private var check:Boolean? = null
    private var randomValues:String?=null
    private lateinit var authRepository: AuthRepository
    private val productApi = RetrofitClient.apiService
    val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityRegisterBinding
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1
    val CHANNEL_ID = "RegisterAccount"
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
            }
        }




        /**
         * Регистрация
         */
        binding.btnSignIn.setOnClickListener {
            val email = binding.etLogin.text?.toString() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""
            val name = binding.etName.text.toString()
            val surname = binding.etSurName.text.toString()
            val telephone = binding.etTelephone.text.toString()
            val isNumber = "[0123456789]".toRegex()
            check = emailMap.get(binding.etLogin.text.toString())
            if (check==null) check = false
            with(binding) {
                if (!check!!){
                    binding.etLogin.error="erroreeeee"
                    return@setOnClickListener
                }
                if (email.trim().isEmpty()) {
                    validaciaIsEmpty(binding.etLogin)
                    return@setOnClickListener
                }
                if (!email.contains("@mail.ru") &&
                    !email.contains("@gmail.com") &&
                    !email.contains("@yandex.ru") &&
                    !email.contains("@mpt.ru") &&
                    !email.contains("@icloud.com")
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
                if (surname.trim().isEmpty()) {
                    etSurName.error = "null"
                    return@setOnClickListener
                }
                if (telephone.trim().isEmpty()) {
                    etTelephone.error = "null"
                    return@setOnClickListener
                }
                if (!validatePassword(password)) {
                    return@setOnClickListener
                }
                if (name.contains(isNumber)) {
                    etName.error = getString(R.string.please_insert_correct_name)
                    return@setOnClickListener
                }
                if (name.length <= 1) {
                    etName.error = getString(R.string.please_insert_correct_name)
                    return@setOnClickListener
                }
                if (surname.contains(isNumber)) {
                    etSurName.error = getString(R.string.please_insert_correct_surname)
                    return@setOnClickListener
                }
                if (!telephone.startsWith("+")) {
                    etTelephone.error = getString(R.string.please_start_with)
                    return@setOnClickListener
                }
                if (!telephone.contains(isNumber)) {
                    etTelephone.error = getString(R.string.please_only_numbers)
                    return@setOnClickListener
                }
                if (telephone.length < 6 || telephone.length > 15) {
                    etTelephone.error = getString(R.string.telephone_must_be_6_to_15_character)
                    return@setOnClickListener
                }


            }
            lifecycleScope.launch {
               withAuthToken { token ->
                   val list = productApi.getUsers(token)
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
                           getString(R.string.client),
                           null
                       )
                       viewModel.insertItem(item)
                       sendNotification(
                           getString(R.string.registerAccount),
                           getString(R.string.SuccesregisterAccount)
                       )
                       startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                       finish()
                   }
               }

            }
        }

        binding.btnArrowBack.setOnClickListener {
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
        }
        binding.btnConfirmEmail.setOnClickListener {
            val email = binding.etLogin.text.toString()

            check = emailMap.get(binding.etLogin.text.toString())
            if (check==null) check=false
            if (check!!) {
                Toast.makeText(this@RegisterActivity,getString(R.string.succesCheckEmail), Toast.LENGTH_SHORT).show()
            } else {
                emailMap.put(email,false)
                Thread(Runnable {
                    sendEmail(email)
                }).start()
                showConfirmEmailDialog()
            }
        }
    }


    fun validaciaIsEmpty(et: EditText) {
        et.error = getString(R.string.nullable)
    }

    /**
     * Функция проверки валидации пароля
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




    fun sendNotification(title: String, description: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = title
            val descriptionText = description
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@RegisterActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            val NOTIFICATION_ID = 1
            notify(NOTIFICATION_ID, builder.build())
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
    @SuppressLint("ResourceAsColor")
    private fun showConfirmEmailDialog() {
        val dialog = AlertDialog.Builder(this@RegisterActivity)
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
                Toast.makeText(this@RegisterActivity,getString(R.string.succesCheckEmail), Toast.LENGTH_SHORT).show()
            }else{
                binding.btnConfirmEmail.setBackgroundResource(R.color.red)
                emailMap.set(binding.etLogin.text.toString(),false)

                Toast.makeText(this@RegisterActivity,getString(R.string.unsuccessfullyCheckEmail),
                    Toast.LENGTH_SHORT).show()
            }

        }
        btnLeave.setOnClickListener {
            customdialog.dismiss()
        }
        customdialog.show()
    }

}