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
import androidx.lifecycle.viewModelScope
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.BrandViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityCreateBrandBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateBrandActivity : BaseActivity() {

    lateinit var binding:ActivityCreateBrandBinding

    val viewModel: BrandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBrandBinding.inflate(layoutInflater)
        setContentView(binding.root)



        /**
         *  Реализация создания брэнда
         */
        binding.btnCreate.setOnClickListener {
            val name = binding.etNameBrand.text.toString().trim()

            if (name.isNotEmpty()) {
                viewModel.checkAndInsertBrand(
                    name = name,
                    onExists = {
                        runOnUiThread {
                            binding.etNameBrand.error = getString(R.string.brand_this_used)
                        }
                    },
                    onSuccess = { brand ->
                        startActivity(
                            Intent(
                                this@CreateBrandActivity,
                                BrandAndCategoryViewActivity::class.java
                            )
                        )
                        finish()
                    }
                )
            } else {
                binding.etNameBrand.error = getString(R.string.brand_this_used)
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