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
import androidx.lifecycle.lifecycleScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.CategoryViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityUpdateCategoryBinding
import com.example.testbundle.db.Category
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.launch

class UpdateCategoryActivity : BaseActivity() {
    lateinit var binding:ActivityUpdateCategoryBinding
    val viewModel : CategoryViewModel by viewModels<CategoryViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = MainDb.getDb(this)
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val id = intent.getIntExtra("category_id",0)
        lifecycleScope.launch {
            val category = db.getDao().getCategoryById(id)
            binding.etNameCategory.setText(category.name)

        }
        /**
         * Кнопка назад возвращает на прошую активити
         */
        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this@UpdateCategoryActivity, BrandAndCategoryViewActivity::class.java))
        }
        /**
         * Реализация обновления категорий
         */
        binding.btnUpdate.setOnClickListener {
            val name = binding.etNameCategory.text.toString().trim()
            val containsNumber = Regex("[0-9]").containsMatchIn(name)
            val currentCategoryId = intent.getIntExtra("category_id", -1) // Получаем ID категории из Intent

            // Валидация ввода
            when {
                name.isEmpty() -> {
                    Toast.makeText(this, R.string.Please_fill_in_all_fields, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                containsNumber -> {
                    Toast.makeText(this, R.string.input_type_formar_text, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Проверка существующих категорий
            db.getDao().getAllCategory().asLiveData().observe(this) { categories ->
                // Проверяем, есть ли категория с таким именем (кроме текущей)
                val categoryExists = categories.any { it.name == name && it.id != currentCategoryId }

                if (categoryExists) {
                    binding.etNameCategory.error = getString(R.string.category_this_used)
                } else {
                    // Если категории с таким именем нет - обновляем
                    val updatedCategory = Category(currentCategoryId, name)
                    viewModel.updateCategory(updatedCategory)

                    startActivity(
                        Intent(
                            this@UpdateCategoryActivity,
                            BrandAndCategoryViewActivity::class.java
                        )
                    )
                    finish() // Закрываем текущую активити
                }
            }
        }
    }


}