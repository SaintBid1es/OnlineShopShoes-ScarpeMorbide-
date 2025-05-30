package com.example.testbundle.Activity.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.CategoryViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityCreateCategoryBinding
import com.example.testbundle.db.Category
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateCategoryActivity : BaseActivity() {
    lateinit var binding:ActivityCreateCategoryBinding
    val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Реализация создания категорий
         */
        binding.btnCreate.setOnClickListener {
            val name = binding.etNameCategory.text.toString().trim()
            val containsNumber = Regex("[0-9]").containsMatchIn(name)

            if (name.isEmpty()) {
                Toast.makeText(this, R.string.Please_fill_in_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (containsNumber) {
                Toast.makeText(this, R.string.input_type_formar_text, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (name.isNotEmpty()) {
                viewModel.checkAndInsertCategory(
                    name = name,
                    onExists = {
                        // Вызывается в UI потоке автоматически (ViewModelScope использует Dispatchers.Main)
                        binding.etNameCategory.error = getString(R.string.category_this_used)
                    },
                    onSuccess = { category ->
                        // Переход на другую активити
                        startActivity(
                            Intent(
                                this@CreateCategoryActivity,
                                BrandAndCategoryViewActivity::class.java
                            )
                        )
                        finish()
                    }
                )
            } else {
                binding.etNameCategory.error = getString(R.string.category_this_used)
            }
        }
        /**
         * Кнопка назад
         */
        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this@CreateCategoryActivity,BrandAndCategoryViewActivity::class.java))
        }

    }


}