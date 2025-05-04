package com.example.testbundle.Activity.Admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.net.toUri

class UpdateProductActivity : BaseActivity() {
    private lateinit var binding: ActivityUpdateProductBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var brandAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var selectedImagePosition = RecyclerView.NO_POSITION
    private lateinit var db: MainDb

    private val imageList = mutableListOf<ProductImage>().apply {
        addAll(listOf(
            R.drawable.shoes1, R.drawable.shoes2, R.drawable.shoes3, R.drawable.shoes4,
            R.drawable.shoes5, R.drawable.shoes6, R.drawable.shoes7, R.drawable.shoes8,
            R.drawable.shoes9, R.drawable.shoes10, R.drawable.shoes11, R.drawable.shoes12
        ).map { ProductImage.DrawableImage(it) })
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openImagePicker() else "showPermissionDeniedMessage"
    }

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { handleSelectedImage(it) } }

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
            checkAndRequestPermission()
        }
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            shouldShowRequestPermissionRationale(permission) -> {

            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        try {
            imagePicker.launch("image/*")
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error launching image picker", e)
            Toast.makeText(this, "Ошибка при открытии галереи", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                // 1. Получаем оригинальное имя файла или генерируем новое
                val originalFileName = withContext(Dispatchers.IO) {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        cursor.moveToFirst()
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) cursor.getString(nameIndex) else null
                    }
                }

                // 2. Сохраняем изображение во внутреннее хранилище
                val savedFile = saveImageToInternalStorage(uri, originalFileName)
                Log.d("ImageSave", "Image saved to: ${savedFile.absolutePath}")

                // 3. Создаем URI через FileProvider
                val fileUri = FileProvider.getUriForFile(
                    this@UpdateProductActivity,
                    "${packageName}.fileprovider",
                    savedFile
                ).also {
                    Log.d("ImageUri", "Created FileProvider URI: $it")
                }

                // 4. Удаляем старое изображение (если было выбрано и это пользовательское изображение)
                if (selectedImagePosition != RecyclerView.NO_POSITION) {
                    val oldImage = imageList[selectedImagePosition]
                    if (oldImage is ProductImage.UriImage) {
                        try {
                            deleteImageFile(oldImage.uri)
                            Log.d("ImageCleanup", "Old image deleted successfully")
                        } catch (e: Exception) {
                            Log.e("ImageCleanup", "Failed to delete old image", e)
                            // Продолжаем работу даже если не удалось удалить старое изображение
                        }
                    }
                }

                // 5. Добавляем новое изображение в список
                val newImage = ProductImage.UriImage(fileUri)
                imageList.add(newImage)
                selectedImagePosition = imageList.size - 1

                // 6. Обновляем UI
                withContext(Dispatchers.Main) {
                    (binding.rcViewImage.adapter as? ProductCreateAdapter)?.apply {
                        notifyItemInserted(imageList.size - 1)
                        setSelectedPosition(selectedImagePosition)
                    }
                }

            } catch (e: IOException) {
                Log.e("ImageError", "File operation failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UpdateProductActivity,
                        "Ошибка сохранения изображения: ${e.message ?: "файл не сохранен"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: SecurityException) {
                Log.e("ImageError", "Security violation", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UpdateProductActivity,
                        "Ошибка доступа к файлам. Проверьте разрешения",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("ImageError", "Unexpected error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UpdateProductActivity,
                        "Неизвестная ошибка при обработке изображения",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun deleteImageFile(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val file = File(uri.path ?: return@withContext)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            Log.e("ImageDelete", "Error deleting image file", e)
        }
    }

    private suspend fun saveImageToInternalStorage(uri: Uri, originalName: String? = null): File =
        withContext(Dispatchers.IO) {
            val imagesDir = File(filesDir, "product_images").apply {
                if (!exists()) mkdirs()
            }

            // Генерируем имя файла с учетом оригинального имени (если есть)
            val fileName = originalName?.takeIf { it.isNotBlank() }?.let {
                if (it.contains('.')) it else "$it.jpg"
            } ?: "img_${System.currentTimeMillis()}.jpg"

            val outputFile = File(imagesDir, fileName)

            try {
                // Читаем и сохраняем изображение
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                } ?: throw IOException("Не удалось открыть исходное изображение")

                // Проверяем результат сохранения
                when {
                    !outputFile.exists() -> throw IOException("Файл не был создан")
                    outputFile.length() == 0L -> {
                        outputFile.delete()
                        throw IOException("Файл сохранен как пустой")
                    }
                    else -> return@withContext outputFile
                }
            } catch (e: Exception) {
                // Удаляем частично сохраненный файл в случае ошибки
                if (outputFile.exists()) outputFile.delete()
                throw e
            }
        }

    private fun setupSpinners() {
        brandAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.brandSpinner.adapter = brandAdapter

        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

        db.getDao().getAllBrand().asLiveData().observe(this) { brands ->
            brandAdapter.clear()
            brandAdapter.addAll(brands.map { it.name })
        }

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

                // Загрузка изображения продукта
                loadProductImage(product)

                // Установка выбранных значений в спиннерах
                setSpinnerSelections(product.brandId, product.category)
            } catch (e: Exception) {
                Log.e("UpdateProduct", "Error loading product", e)
                Toast.makeText(this@UpdateProductActivity,
                    "Ошибка загрузки продукта", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadProductImage(product: Products) {
        // Если есть URI изображения
        product.imageUri?.takeIf { it.isNotEmpty() }?.let { fileName ->
            try {
                val imagesDir = File(filesDir, "product_images")
                val imageFile = File(imagesDir, fileName)

                if (imageFile.exists() && imageFile.length() > 0) {
                    val uri = FileProvider.getUriForFile(
                        this@UpdateProductActivity,
                        "${packageName}.fileprovider",
                        imageFile
                    )
                    imageList.add(ProductImage.UriImage(uri))
                    selectedImagePosition = imageList.size - 1
                } else {
                    Log.w("ImageLoad", "File not found or empty: ${imageFile.absolutePath}")
                    // Используем стандартное изображение и помечаем для обновления
                    imageList.add(ProductImage.DrawableImage(R.drawable.avatarmen))
                    selectedImagePosition = imageList.size - 1
                    // Удаляем несуществующую ссылку из базы данных
                    lifecycleScope.launch {
                        db.getDao().updateProductImage(product.id!!, 0, null)
                    }
                }
            } catch (e: Exception) {
                Log.e("ImageLoad", "Error loading image URI", e)
                imageList.add(ProductImage.DrawableImage(R.drawable.apple))
                selectedImagePosition = imageList.size - 1
            }
        } ?: run {
            // Если есть ID ресурса
            if (product.imageId != 0) {
                updateSelectedImage(product.imageId)
            } else {
                // Если нет изображения, добавляем placeholder
                imageList.add(ProductImage.DrawableImage(R.drawable.star_ic))
                selectedImagePosition = imageList.size - 1
            }
        }

        runOnUiThread {
            (binding.rcViewImage.adapter as? ProductCreateAdapter)?.apply {
                notifyDataSetChanged()
                setSelectedPosition(selectedImagePosition)
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
        val name = binding.etNameProduct.text.toString().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Product name cannot be empty")

        val cost = try {
            binding.etCost.text.toString().toDouble()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid price format", e)
        }

        val amount = try {
            binding.etAmount.text.toString().toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid amount format", e)
        }

        val brandName = binding.brandSpinner.selectedItem?.toString()
            ?: throw IllegalStateException("Brand not selected")
        val categoryName = binding.categorySpinner.selectedItem?.toString()
            ?: throw IllegalStateException("Category not selected")

        val brandId = db.getDao().getBrandByName(brandName)?.id
            ?: throw IllegalArgumentException("Brand '$brandName' not found")
        val categoryId = db.getDao().getCategoryByName(categoryName)?.id
            ?: throw IllegalArgumentException("Category '$categoryName' not found")

        if (selectedImagePosition == RecyclerView.NO_POSITION) {
            throw IllegalStateException("Please select an image")
        }

        val selectedImage = imageList[selectedImagePosition]
        return when (selectedImage) {
            is ProductImage.DrawableImage -> Products(
                id = productId,
                name = name,
                cost = cost,
                description = binding.etDescription.text.toString(),
                size = null,
                imageId = selectedImage.resId,
                brandId = brandId,
                category = categoryId,
                amount = amount,
                imageUri = null
            )
            is ProductImage.UriImage -> {
                val fileName = File(selectedImage.uri.path ?: "").name
                Products(
                    id = productId,
                    name = name,
                    cost = cost,
                    description = binding.etDescription.text.toString(),
                    size = null,
                    imageId = 0,
                    brandId = brandId,
                    category = categoryId,
                    amount = amount,
                    imageUri = fileName
                )
            }
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
                showError(R.string.please_select_an_image)
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
        val errorText = message?.let { "${getString(stringRes)}: $it" } ?: getString(stringRes)
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