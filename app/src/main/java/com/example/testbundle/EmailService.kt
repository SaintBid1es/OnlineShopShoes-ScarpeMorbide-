package com.example.testbundle



import android.app.IntentService
import android.content.Intent

class EmailService : IntentService("EmailService") {

    override fun onHandleIntent(intent: Intent?) {
        sendEmail()
    }

    private fun sendEmail() {
        // Ваш код отправки сообщения электронной почты
    }
}