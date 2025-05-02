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
import com.example.testbundle.BrandViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityCreateBrandBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.MainDb

class CreateBrandActivity : BaseActivity() {

    lateinit var binding:ActivityCreateBrandBinding

    val viewModel: BrandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBrandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = MainDb.getDb(this)


        /**
         *  Реализация создания брэнда
         */
        binding.btnCreate.setOnClickListener {
            val name = binding.etNameBrand.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this,
                    getString(R.string.Please_fill_in_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.getDao().getAllBrand().asLiveData().observe(this) { brands ->
                // Проверяем, есть ли бренд с таким именем
                val brandExists = brands.any { it.name == name }

                if (brandExists) {
                    binding.etNameBrand.error = getString(R.string.brand_this_used)
                } else {
                    // Если бренда нет, создаем его
                    val newBrand = Brand(
                        null,
                        name
                    )
                    viewModel.insertBrand(newBrand)

                    startActivity(
                        Intent(
                            this@CreateBrandActivity,
                            BrandAndCategoryViewActivity::class.java
                        )
                    )
                }
            }
        }
        /**
         * Кнопка назад
         */
        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this@CreateBrandActivity,BrandAndCategoryViewActivity::class.java))
        }
    }




}