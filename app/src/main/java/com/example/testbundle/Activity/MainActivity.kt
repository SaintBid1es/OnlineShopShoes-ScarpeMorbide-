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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.User.ProfileActivity
import com.example.testbundle.LocaleUtils
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityMainBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.first
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


class MainActivity : BaseActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding null")
    private var randomValues:String?=null

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
//        binding.arrowLeave.setOnClickListener {
//            exitApp(this)
//        }

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

//        binding.btnLanguage.setOnClickListener {
//            changeLanguage()
//        }
        binding.forgotPasswordLink.setOnClickListener {
                showConfirmEmailDialog()
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
  fun sendEmailNewPassword(toEmail: String,password:String) {
        try {
            val props = Properties()
            props.setProperty("mail.transport.protocol", "smtp")
            props.setProperty("mail.host", "smtp.gmail.com")
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            val session = Session.getDefaultInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    //от кого
                    return PasswordAuthentication("isip_m.a.vesenkov@mpt.ru", "mbmtqsqhtxxwurzn")
                }
            })
            val message = MimeMessage(session)//от кого
            message.setFrom(InternetAddress("isip_m.a.vesenkov@mpt.ru"))//куда
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
            message.subject = "This is New Password"
            message.setText(password)

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
        val dialog = AlertDialog.Builder(this@MainActivity)
        val dialogView = layoutInflater.inflate(R.layout.resetpassword_item, null)
        val etCode = dialogView.findViewById<EditText>(R.id.etCode)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnSendMessage = dialogView.findViewById<Button>(R.id.btnSendMessage)
        val btnLeave = dialogView.findViewById<ImageButton>(R.id.btnLeave)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        val customdialog = dialog.create()
        customdialog.show()
        btnSendMessage.setOnClickListener {
            Thread(Runnable{
                val email = etEmail.text.toString()
                sendEmail(email)
            }).start()
        }
        btnConfirm.setOnClickListener {
            val code = etCode.text?.toString() ?: ""
            if (code==randomValues){
                val newPassword = newPasswordEmail(11)
                Thread(Runnable {
                    sendEmailNewPassword(etEmail.text.toString(), newPassword)
                }).start()
                updatePasswordForEmail(etEmail.text.toString(),newPassword)
                customdialog.dismiss()
                Toast.makeText(this@MainActivity,getString(R.string.succesCheckEmail), Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity,getString(R.string.unsuccessfullyCheckEmail),
                    Toast.LENGTH_SHORT).show()
            }

        }
        btnLeave.setOnClickListener {
            customdialog.dismiss()
        }

    }
    private fun updatePasswordForEmail(email: String,password:String) {
        val db = MainDb.getDb(this)
        db.getDao().getAllItems().asLiveData().observe(this) { list ->
            val user = list.find { it.email == email  }
            user!!.password = password
        }
    }
    fun newPasswordEmail(len:Int) :String{
         val DATA: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£$%&/=@#"
            val sb = StringBuilder(len)
            for (i in 0..<len) {
                sb.append(DATA[Random.nextInt(DATA.length)])
            }
        return sb.toString()
    }




}
