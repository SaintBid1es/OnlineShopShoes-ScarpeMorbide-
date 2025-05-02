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
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.CategoryViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityCreateCategoryBinding
import com.example.testbundle.db.Category
import com.example.testbundle.db.MainDb

class CreateCategoryActivity : BaseActivity() {
    lateinit var binding:ActivityCreateCategoryBinding
    val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = MainDb.getDb(this)
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

            db.getDao().getAllCategory().asLiveData().observe(this) { categories ->
                // Проверяем, есть ли категория с таким именем
                val categoryExists = categories.any { it.name == name }

                if (categoryExists) {
                    binding.etNameCategory.error = getString(R.string.category_this_used)
                } else {
                    // Если категории нет, создаем её
                    val newCategory = Category(
                        null,
                        name
                    )
                    viewModel.insertCategory(newCategory)

                    startActivity(
                        Intent(
                            this@CreateCategoryActivity,
                            BrandAndCategoryViewActivity::class.java
                        )
                    )
                    finish() // Рекомендуется закрыть текущую активити
                }
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