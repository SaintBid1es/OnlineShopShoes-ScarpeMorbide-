package com.example.testbundle.Activity.Admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Adapter.ProductCreateAdapter
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityUpdateProductBinding
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import kotlinx.coroutines.launch

class UpdateProductActivity : BaseActivity() {
    private lateinit var binding: ActivityUpdateProductBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var brandAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var selectedImagePosition = RecyclerView.NO_POSITION
    private lateinit var db: MainDb

    private val imageIdList = listOf(
        R.drawable.shoes1, R.drawable.shoes2, R.drawable.shoes3, R.drawable.shoes4,
        R.drawable.shoes5, R.drawable.shoes6, R.drawable.shoes7, R.drawable.shoes8,
        R.drawable.shoes9, R.drawable.shoes10, R.drawable.shoes11, R.drawable.shoes12
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = MainDb.getDb(this)
        binding = ActivityUpdateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("product_id", 0).takeIf { it != 0 } ?: run {
            Toast.makeText(this, getString(R.string.invalid_product_id), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupImageRecycler()
        setupSpinners()
        loadProductData(productId)
        setupUpdateButton(productId)
        setupBackButton()
    }

    private fun setupImageRecycler() {
        binding.rcViewImage.apply {
            adapter = ProductCreateAdapter(imageIdList, selectedImagePosition) { position ->
                selectedImagePosition = position
            }
            layoutManager = LinearLayoutManager(this@UpdateProductActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupSpinners() {
        /**
         * Инициализация адаптеров
         */
        brandAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.brandSpinner.adapter = brandAdapter

        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

        /**
         * Загрузка брендов
         */
        db.getDao().getAllBrand().asLiveData().observe(this) { brands ->
            brandAdapter.clear()
            brandAdapter.addAll(brands.map { it.name })
        }

        /**
         * Загрузка категорий
         */
        db.getDao().getAllCategory().asLiveData().observe(this) { categories ->
            categoryAdapter.clear()
            categoryAdapter.addAll(categories.map { it.name })
        }
    }

    /**
     *Функция Загрузки Данных продукта
     */
    private fun loadProductData(productId: Int) {
        lifecycleScope.launch {
            val product = db.getDao().getProductById(productId) ?: run {
                Toast.makeText(this@UpdateProductActivity,
                    getString(R.string.product_not_found), Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            binding.etNameProduct.setText(product.name)
            binding.etDescription.setText(product.description)
            binding.etCost.setText(product.cost.toString())


            selectedImagePosition = imageIdList.indexOf(product.imageId).takeIf { it != -1 } ?: 0
            (binding.rcViewImage.adapter as? ProductCreateAdapter)?.setSelectedPosition(selectedImagePosition)


            db.getDao().getBrandById(product.brandId)?.name?.let { brandName ->
                val position = brandAdapter.getPosition(brandName)
                if (position >= 0) binding.brandSpinner.setSelection(position)
            }

            db.getDao().getCategoryById(product.category)?.name?.let { categoryName ->
                val position = categoryAdapter.getPosition(categoryName)
                if (position >= 0) binding.categorySpinner.setSelection(position)
            }
        }
    }

    /**
     * Функция обновления данных
     */
    private fun setupUpdateButton(productId: Int) {
        binding.btnUpdate.setOnClickListener {
            if (!validateInput()) return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val brand = db.getDao().getBrandByName(binding.brandSpinner.selectedItem.toString())?.id
                        ?: throw Exception("Brand not found")
                    val category = db.getDao().getCategoryByName(binding.categorySpinner.selectedItem.toString())?.id
                        ?: throw Exception("Category not found")

                    val updatedProduct = Products(
                        id = productId,
                        name = binding.etNameProduct.text.toString(),
                        cost = binding.etCost.text.toString().toDouble(),
                        description = binding.etDescription.text.toString(),
                        null,
                        imageId = imageIdList[selectedImagePosition],
                        brandId = brand,
                        category = category
                    )

                    viewModel.updateProduct(updatedProduct)
                    Toast.makeText(this@UpdateProductActivity,
                        getString(R.string.product_updated), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@UpdateProductActivity, ListProductAdminActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@UpdateProductActivity,  getString(R.string.error, e.message), Toast.LENGTH_SHORT).show()
                    Log.e("UpdateProduct", "Error updating product", e)
                }
            }
        }
    }

    /**
     * Функция проверка на валидацию
     */
    private fun validateInput(): Boolean {
        return when {
            binding.etNameProduct.text.isNullOrBlank() -> {
                showError(R.string.input_name)
                false
            }
            binding.etDescription.text.isNullOrBlank() -> {
                showError(R.string.input_description)
                false
            }
            binding.etCost.text.isNullOrBlank() -> {
                showError(R.string.input_cost)
                false
            }
            !binding.etCost.text.toString().contains(".") -> {
                showError(R.string.input_type_double)
                false
            }
            selectedImagePosition == RecyclerView.NO_POSITION -> {
                Toast.makeText(this, getString(R.string.please_select_an_image), Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showError(stringRes: Int) {
        Toast.makeText(this, getString(stringRes), Toast.LENGTH_SHORT).show()
    }

    private fun setupBackButton() {
        binding.btnArrowBack.setOnClickListener {
            finish()
        }
    }
}