package com.example.testbundle.Activity.User

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.MainActivity
import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityForgenPasswordBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random

class ForgenPasswordActivity : AppCompatActivity() {
    private var emailMap = mutableMapOf<String, Boolean>()
    private var check: Boolean? = null
    private var randomValues: String? = null
    lateinit var binding: ActivityForgenPasswordBinding

    val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgenPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnCheckEmail.setOnClickListener {
            val email = binding.etLogin.text.toString().trim()

            // Add email validation
            if (email.isEmpty()) {
                binding.etLogin.error = getString(R.string.email_and_password_cannot_be_empty)
                return@setOnClickListener
            }



            check = emailMap[email]
            if (check == true) {
                Toast.makeText(this, getString(R.string.succesCheckEmail), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    val db = MainDb.getDb(this@ForgenPasswordActivity)
                    db.getDao().getUserByEmail(email)
                }
                if (user == null) {
                    binding.etLogin.error = "getString(R.string.email_not_registered)"
                } else {
                    emailMap[email] = false
                    sendEmailInBackground(email)
                    showConfirmEmailDialog()
                }
            }

        }

        binding.btnConfirm.setOnClickListener {
            val email = binding.etLogin.text.toString().trim()
            check = emailMap[email]

            if (check != true) {
                Toast.makeText(this, "Не подтвержденная почта", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPassword = newPasswordEmail(11)
            sendNewPasswordInBackground(email, newPassword)
            updatePasswordForEmail(email, newPassword)

            Toast.makeText(this, "Новый пароль отправлен на почту", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendEmailInBackground(email: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                randomValues = Random.nextInt(1000, 9999).toString()
                sendEmail(email)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ForgenPasswordActivity,
                        "Код подтверждения отправлен", Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ForgenPasswordActivity,
                        "Ошибка отправки: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("EmailError", "Failed to send email", e)
            }
        }
    }

    private fun sendNewPasswordInBackground(email: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                sendEmailNewPassword(email, password)
            } catch (e: Exception) {
                Log.e("EmailError", "Failed to send new password", e)
            }
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

    fun sendEmailNewPassword(toEmail: String, password: String) {
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

    fun newPasswordEmail(len: Int): String {
        val DATA: String =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£$%&/=@#"
        val sb = StringBuilder(len)
        for (i in 0..<len) {
            sb.append(DATA[Random.nextInt(DATA.length)])
        }
        return sb.toString()
    }

    private fun updatePasswordForEmail(email: String, password: String) {
        val db = MainDb.getDb(this)
        db.getDao().getAllItems().asLiveData().observe(this) { list ->
            val user = list.find {
                it.email == email

            }
            val correctUser = Item(user!!.id,password,user.Name,user.SurName,user.email,user.telephone,user.speciality)
            viewModel.updateItem(correctUser)
        }

    } @SuppressLint("ResourceAsColor")
    private fun showConfirmEmailDialog() {
        val dialog = AlertDialog.Builder(this@ForgenPasswordActivity)
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

                emailMap.set(binding.etLogin.text.toString(),true)

                customdialog.dismiss()
                Toast.makeText(this@ForgenPasswordActivity,getString(R.string.succesCheckEmail), Toast.LENGTH_SHORT).show()
            }else{

                emailMap.set(binding.etLogin.text.toString(),false)

                Toast.makeText(this@ForgenPasswordActivity,getString(R.string.unsuccessfullyCheckEmail),
                    Toast.LENGTH_SHORT).show()
            }

        }
        btnLeave.setOnClickListener {
            customdialog.dismiss()
        }
        customdialog.show()
    }

}