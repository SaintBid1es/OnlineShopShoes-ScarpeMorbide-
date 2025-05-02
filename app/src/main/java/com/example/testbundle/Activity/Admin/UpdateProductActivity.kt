package com.example.testbundle.Activity.Admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Adapter.ProductCreateAdapter
import com.example.testbundle.ProductImage
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityUpdateProductBinding
import com.example.testbundle.db.ImageEntity
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import kotlinx.coroutines.launch
import java.io.IOException

class UpdateProductActivity : BaseActivity() {
    private lateinit var binding: ActivityUpdateProductBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var brandAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var selectedImagePosition = RecyclerView.NO_POSITION
    private lateinit var db: MainDb

    // Список изображений (ресурсы + загруженные)
    private val imageList = mutableListOf<ProductImage>().apply {
        addAll(listOf(
            R.drawable.shoes1, R.drawable.shoes2, /* ... */
        ).map { ProductImage.DrawableImage(it) })
    }

    // Контракт для выбора изображения
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleSelectedImage(it) }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = MainDb.getDb(this)
        binding = ActivityUpdateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("product_id", 0).takeIf { it != 0 } ?: run {
            showErrorAndFinish(R.string.invalid_product_id)
            return
        }

        setupUI()
        loadProductData(productId)
        setupUpdateButton(productId)
    }

    private fun setupUI() {
        setupImageRecycler()
        setupSpinners()
        setupButtons()
    }

    private fun setupImageRecycler() {
        binding.rcViewImage.apply {
            adapter = ProductCreateAdapter(imageList, selectedImagePosition) { position ->
                selectedImagePosition = position
            }
            layoutManager = LinearLayoutManager(this@UpdateProductActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.btnImage.setOnClickListener {
            imagePicker.launch("image/*")
        }
    }


    private fun handleSelectedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Получаем имя файла для отображения
                val fileName = getFileNameFromUri(uri)

                // Добавляем новое изображение в список
                imageList.add(ProductImage.UriImage(uri))

                // Обновляем адаптер
                (binding.rcViewImage.adapter as? ProductCreateAdapter)?.apply {
                    notifyItemInserted(imageList.size - 1)
                    setSelectedPosition(imageList.size - 1)
                    selectedImagePosition = imageList.size - 1
                }
            } catch (e: Exception) {
                Log.e("ImagePicker", "Error handling image", e)
                Toast.makeText(this@UpdateProductActivity,
                    getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getFileNameFromUri(uri: Uri): String {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex >= 0) cursor.getString(displayNameIndex) else "image_${System.currentTimeMillis()}"
        } ?: "image_${System.currentTimeMillis()}"
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

    private fun setupButtons() {
        binding.btnArrowBack.setOnClickListener { finish() }
    }

    private fun loadProductData(productId: Int) {
        lifecycleScope.launch {
            try {
                val product = db.getDao().getProductById(productId) ?: run {
                    showErrorAndFinish(R.string.product_not_found)
                    return@launch
                }

                with(binding) {
                    etNameProduct.setText(product.name)
                    etDescription.setText(product.description)
                    etCost.setText(product.cost.toString())
                    etAmount.setText(product.amount.toString())
                }



                // Установка выбранных значений в спиннерах
                setSpinnerSelections(product.brandId, product.category)
            } catch (e: Exception) {
                Log.e("UpdateProduct", "Error loading product", e)
                Toast.makeText(this@UpdateProductActivity,
                    "error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSelectedImage(imageId: Int) {
        selectedImagePosition = imageList.indexOfFirst {
            it is ProductImage.DrawableImage && it.resId == imageId
        }.takeIf { it != -1 } ?: 0

        (binding.rcViewImage.adapter as? ProductCreateAdapter)?.setSelectedPosition(selectedImagePosition)
    }

    private fun setSpinnerSelections(brandId: Int, categoryId: Int) {
        lifecycleScope.launch {
            db.getDao().getBrandById(brandId)?.name?.let { brandName ->
                val position = brandAdapter.getPosition(brandName)
                if (position >= 0) binding.brandSpinner.setSelection(position)
            }

            db.getDao().getCategoryById(categoryId)?.name?.let { categoryName ->
                val position = categoryAdapter.getPosition(categoryName)
                if (position >= 0) binding.categorySpinner.setSelection(position)
            }
        }
    }

    private fun setupUpdateButton(productId: Int) {
        binding.btnUpdate.setOnClickListener {
            if (!validateInput()) return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val updatedProduct = createUpdatedProduct(productId)
                    viewModel.updateProduct(updatedProduct)
                    showSuccessAndFinish(R.string.product_updated)
                } catch (e: Exception) {
                    showError(R.string.error, e.message)
                    Log.e("UpdateProduct", "Error updating product", e)
                }
            }
        }
    }

    private suspend fun createUpdatedProduct(productId: Int): Products {
        val brand = db.getDao().getBrandByName(binding.brandSpinner.selectedItem.toString())?.id
            ?: throw Exception("Brand not found")
        val category = db.getDao().getCategoryByName(binding.categorySpinner.selectedItem.toString())?.id
            ?: throw Exception("Category not found")

        val imageId = when (val selectedImage = imageList[selectedImagePosition]) {
            is ProductImage.DrawableImage -> selectedImage.resId
            is ProductImage.UriImage -> saveImageAndGetId(selectedImage.uri)
        }

        return Products(
            id = productId,
            name = binding.etNameProduct.text.toString(),
            cost = binding.etCost.text.toString().toDouble(),
            description = binding.etDescription.text.toString(),
            null,
            imageId = imageId,
            brandId = brand,
            category = category,
            amount = binding.etAmount.text.toString().toInt()
        )
    }

    private suspend fun saveImageAndGetId(uri: Uri): Int {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val imageEntity = ImageEntity(imageData = bytes)
                db.getDao().insertImage(imageEntity).toInt()
            } ?: throw IOException("Failed to read image")
        } catch (e: Exception) {
            Log.e("ImageSave", "Error saving image", e)
            throw e
        }
    }

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
            binding.etCost.text.isNullOrBlank() || !isValidPrice(binding.etCost.text.toString()) -> {
                showError(R.string.error)
                false
            }
            selectedImagePosition == RecyclerView.NO_POSITION -> {
                showError(R.string.error)
                false
            }
            else -> true
        }
    }

    private fun isValidPrice(price: String): Boolean {
        return try {
            price.toDouble() > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun showError(stringRes: Int, message: String? = null) {
        val errorText = if (message != null) {
            "${getString(stringRes)}: $message"
        } else {
            getString(stringRes)
        }
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorAndFinish(stringRes: Int) {
        showError(stringRes)
        finish()
    }

    private fun showSuccessAndFinish(stringRes: Int) {
        Toast.makeText(this, getString(stringRes), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ListProductAdminActivity::class.java))
        finish()
    }
}