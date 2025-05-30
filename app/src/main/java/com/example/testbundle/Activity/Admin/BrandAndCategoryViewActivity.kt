package com.example.testbundle.Activity.Admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.Adapter.AccountCardAdapter
import com.example.testbundle.Adapter.BrandAdapter
import com.example.testbundle.Adapter.CategoryAdapter
import com.example.testbundle.BrandViewModel
import com.example.testbundle.CategoryViewModel
import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityBrandAndCategoryViewBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.launch

class BrandAndCategoryViewActivity : BaseActivity() {
    lateinit var binding:ActivityBrandAndCategoryViewBinding
    val viewModelCategory : CategoryViewModel by viewModels()
    val viewModelBrand : BrandViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrandAndCategoryViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rcViewBrand.layoutManager = LinearLayoutManager(this)
        binding.rcViewCategory.layoutManager = LinearLayoutManager(this)

        /**
         * Навигационная панель
         */
        binding.imgExit.setOnClickListener {
            startActivity(Intent(this@BrandAndCategoryViewActivity,ListProductActivity::class.java))
        }
        binding.imgCreateBrand.setOnClickListener {
            startActivity(Intent(this@BrandAndCategoryViewActivity,CreateBrandActivity::class.java))
        }
        binding.imgCreateCategory.setOnClickListener {
            startActivity(Intent(this@BrandAndCategoryViewActivity,CreateCategoryActivity::class.java))
        }
        /**
         * Вывод списка брэндов
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModelBrand?.stateBrand?.collect{
                    onUpdateViewBrand(it)
                }
            }
        }
        /**
         * Вывод списка категорий
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModelCategory?.stateCategory?.collect{
                    onUpdateViewCategory(it)
                }
            }
        }

    }

    /**
     * //Вывод брэнда с помощью recyclerViewAdapter
     */
    private fun onUpdateViewBrand(entities: List<Brand>) {
        binding.apply {

            rcViewBrand.adapter = BrandAdapter(entities, onEdit = {
                intent = Intent(this@BrandAndCategoryViewActivity, UpdateBrandActivity::class.java).apply {
                    putExtra("brand_id",it.id)
                }
                startActivity(intent)
            },
                onDelete = { id ->
                    viewModelBrand.deleteBrand(id)
                })

        }
    }

    /**
     *  //Вывод категорий с помощью recyclerViewAdapter
     */
    private fun onUpdateViewCategory(entities: List<Category>) {
        binding.apply {

            rcViewCategory.adapter = CategoryAdapter(entities, onEdit = {
                intent = Intent(this@BrandAndCategoryViewActivity, UpdateCategoryActivity::class.java).apply {
                    putExtra("category_id",it.id)
                }
                startActivity(intent)
            },
                onDelete = { id ->
                    viewModelCategory.deleteCategory(id)
                })

        }
    }

}