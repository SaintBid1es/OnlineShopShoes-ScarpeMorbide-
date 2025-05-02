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
import com.example.testbundle.BrandViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityUpdateBrandBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import kotlinx.coroutines.launch


class UpdateBrandActivity : BaseActivity() {
    lateinit var binding:ActivityUpdateBrandBinding
    val viewModel : BrandViewModel by viewModels<BrandViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = MainDb.getDb(this)
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBrandBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("brand_id",0)
        lifecycleScope.launch {
            val brand = db.getDao().getBrandById(id)
            binding.etNameBrand.setText(brand!!.name)

        }
        /**
         * Кнопка обновление информации для брэнда
         */
        binding.btnUpdate.setOnClickListener {
            val name = binding.etNameBrand.text.toString().trim()
            val containsNumber = Regex("[0-9]").containsMatchIn(name)
            val currentBrandId = intent.getIntExtra("brand_id", -1) // Получаем ID из Intent

            // Валидация ввода
            when {
                name.isEmpty() -> {
                    Toast.makeText(this, R.string.Please_fill_in_all_fields, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                containsNumber -> {
                    Toast.makeText(this, R.string.string, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Проверка существующих брендов
            db.getDao().getAllBrand().asLiveData().observe(this) { brands ->
                // Проверяем, есть ли бренд с таким именем (кроме текущего)
                val brandExists = brands.any { it.name == name && it.id != currentBrandId }

                if (brandExists) {
                    binding.etNameBrand.error = getString(R.string.brand_this_used)
                } else {
                    // Если бренда с таким именем нет - обновляем
                    val updatedBrand = Brand(currentBrandId, name)
                    viewModel.updateBrand(updatedBrand)

                    startActivity(
                        Intent(
                            this@UpdateBrandActivity,
                            BrandAndCategoryViewActivity::class.java
                        )
                    )
                    finish()
                }
            }
        }
        /**
         * Кнопка назад возвращает на прошлую активити
         */
        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this@UpdateBrandActivity, BrandAndCategoryViewActivity::class.java))
        }
    }


}