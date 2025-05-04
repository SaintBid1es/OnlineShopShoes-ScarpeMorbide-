package com.example.testbundle.Activity.Admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.example.testbundle.databinding.ActivityCreateProductBinding
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CreateProductActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateProductBinding
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

    // Контракты для запросов
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            showPermissionDeniedMessage()
        }
    }

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleSelectedImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = MainDb.getDb(this)
        binding = ActivityCreateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupImageRecycler()
        setupSpinners()
        setupCreateButton()
        setupBackButton()

        binding.btnImage.setOnClickListener {
            checkAndRequestPermission()
        }
    }

    private fun setupCreateButton() {
        binding.btnCreate.setOnClickListener {
            if (!validateInput()) return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val product = createProductFromInput()
                    viewModel.insertProduct(product)
                    showSuccessAndFinish(R.string.product_created_successfully)
                } catch (e: Exception) {
                    showError(R.string.error, e.message)
                    Log.e("CreateProduct", "Error creating product", e)
                }
            }
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
                showPermissionExplanation()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showPermissionExplanation() {
        Toast.makeText(
            this,
            "Для выбора изображения необходимо разрешение на доступ к медиафайлам",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "Разрешение отклонено. Вы можете изменить это в настройках приложения",
            Toast.LENGTH_LONG
        ).show()
        openAppSettings()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
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
                // Сохраняем изображение во внутреннее хранилище
                val imageFile = saveImageToInternalStorage(uri)
                Log.d("ImageSave", "Image saved to: ${imageFile.absolutePath}")

                // Создаем URI через FileProvider
                val fileUri = FileProvider.getUriForFile(
                    this@CreateProductActivity,
                    "${packageName}.fileprovider",
                    imageFile
                )

                // Добавляем в список
                imageList.add(ProductImage.UriImage(fileUri))

                // Обновляем UI
                runOnUiThread {
                    (binding.rcViewImage.adapter as? ProductCreateAdapter)?.apply {
                        notifyItemInserted(imageList.size - 1)
                        selectedImagePosition = imageList.size - 1
                        setSelectedPosition(selectedImagePosition)
                    }
                }
            } catch (e: Exception) {
                Log.e("ImageError", "Failed to handle image", e)
                runOnUiThread {
                    Toast.makeText(
                        this@CreateProductActivity,
                        "Ошибка обработки изображения: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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


    private fun setupImageRecycler() {
        binding.rcViewImage.apply {
            adapter = ProductCreateAdapter(imageList, selectedImagePosition) { position ->
                selectedImagePosition = position
                Log.d("ImageSelection", "Selected image at position $position")
            }
            layoutManager = LinearLayoutManager(this@CreateProductActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private suspend fun createProductFromInput(): Products {
        // Получаем и валидируем данные
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

        // Получаем выбранные бренд и категорию
        val brandName = binding.brandSpinner.selectedItem?.toString()
            ?: throw IllegalStateException("Brand not selected")
        val categoryName = binding.categorySpinner.selectedItem?.toString()
            ?: throw IllegalStateException("Category not selected")

        // Получаем ID бренда и категории
        val brandId = db.getDao().getBrandByName(brandName)?.id
            ?: throw IllegalArgumentException("Brand '$brandName' not found")
        val categoryId = db.getDao().getCategoryByName(categoryName)?.id
            ?: throw IllegalArgumentException("Category '$categoryName' not found")

        // Проверяем, что изображение выбрано
        if (selectedImagePosition == RecyclerView.NO_POSITION) {
            throw IllegalStateException("Please select an image")
        }

        // Создаем продукт с правильным URI
        val selectedImage = imageList[selectedImagePosition]
        return when (selectedImage) {
            is ProductImage.DrawableImage -> Products(
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
                    name = name,
                    cost = cost,
                    description = binding.etDescription.text.toString(),
                    size = null,
                    imageId = 0,
                    brandId = brandId,
                    category = categoryId,
                    amount = amount,
                    imageUri = fileName // Сохраняем только имя файла
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

    private fun showSuccessAndFinish(stringRes: Int) {
        Toast.makeText(this, getString(stringRes), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ListProductAdminActivity::class.java))
        finish()
    }

    private fun setupBackButton() {
        binding.btnArrowBack.setOnClickListener {
            finish()
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
}