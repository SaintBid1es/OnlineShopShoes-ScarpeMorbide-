package com.example.shoesonlineshop.activity

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testbundle.LocaleUtils


open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val array:Array<Int?> = arrayOf(1,2,4,5)
        array.reverse()

    }
    /**
     * Смена языка
     */
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = LocaleUtils.getSavedLanguage(newBase)
        val context = LocaleUtils.updateLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }
}
