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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Adapter.ProductCreateAdapter
import com.example.testbundle.ProductImage
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityCreateProductBinding
import com.example.testbundle.db.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreateProductActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateProductBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var brandAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var selectedImagePosition = RecyclerView.NO_POSITION

    // Теперь список будет хранить URL изображений с сервера (и Drawable для примера)
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
        if (isGranted) openImagePicker() else showPermissionDeniedMessage()
    }

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { uploadImageToServer(it) } }

    private val productApi = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupImageRecycler()
        setupSpinners()
        setupCreateButton()
        setupBackButton()

        binding.btnImage.setOnClickListener { checkAndRequestPermission() }
        binding.imgCreateCategory.setOnClickListener {
            startActivity(Intent(this, BrandAndCategoryViewActivity::class.java))
        }
        binding.imgCreateBrand.setOnClickListener {
            startActivity(Intent(this, BrandAndCategoryViewActivity::class.java))
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
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> openImagePicker()
            shouldShowRequestPermissionRationale(permission) -> showPermissionExplanation()
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun showPermissionExplanation() {
        Toast.makeText(this, "Для выбора изображения необходимо разрешение на доступ к медиафайлам", Toast.LENGTH_LONG).show()
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Разрешение отклонено. Вы можете изменить это в настройках приложения", Toast.LENGTH_LONG).show()
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

    // Новый метод: загрузка изображения на сервер
    private fun uploadImageToServer(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Получаем файл из Uri (копируем в cacheDir)
                val file = withContext(Dispatchers.IO) {
                    val inputStream = contentResolver.openInputStream(uri)
                    val tempFile = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                    inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
                    tempFile
                }
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = productApi.uploadImage(multipartBody)

                if (response.isSuccessful) {
                    val url = response.body()?.url
                    if (url != null) {
                        imageList.add(ProductImage.UrlImage(url))
                        withContext(Dispatchers.Main) {
                            (binding.rcViewImage.adapter as? ProductCreateAdapter)?.notifyItemInserted(imageList.size - 1)
                            selectedImagePosition = imageList.size - 1
                            (binding.rcViewImage.adapter as? ProductCreateAdapter)?.setSelectedPosition(selectedImagePosition)
                        }
                    } else {
                        showError(R.string.error, "Ошибка: сервер не вернул URL")
                    }
                } else {
                    showError(R.string.error, "Ошибка загрузки изображения: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("UploadImage", "Error uploading image", e)
                showError(R.string.error, "Ошибка загрузки изображения: ${e.localizedMessage}")
            }
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
        val name = binding.etNameProduct.text.toString().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Product name cannot be empty")
        val cost = binding.etCost.text.toString().toDoubleOrNull() ?: throw IllegalArgumentException("Invalid price format")
        val amount = binding.etAmount.text.toString().toIntOrNull() ?: throw IllegalArgumentException("Invalid amount format")
        val brandName = binding.brandSpinner.selectedItem?.toString() ?: throw IllegalStateException("Brand not selected")
        val categoryName = binding.categorySpinner.selectedItem?.toString() ?: throw IllegalStateException("Category not selected")

        val brandId = productApi.getBrandByName(brandName)?.id ?: throw IllegalArgumentException("Brand '$brandName' not found")
        val categoryId = productApi.getCategoryByName(categoryName)?.id ?: throw IllegalArgumentException("Category '$categoryName' not found")

        if (selectedImagePosition == RecyclerView.NO_POSITION) {
            throw IllegalStateException("Please select an image")
        }

        val selectedImage = imageList[selectedImagePosition]
        return when (selectedImage) {
            is ProductImage.DrawableImage -> Products(
                name = name,
                cost = cost,
                description = binding.etDescription.text.toString(),
                size = null,
                imageid = selectedImage.resId,
                brandid = brandId,
                categoryid = categoryId,
                amount = amount,
                imageuri = null
            )
            is ProductImage.UrlImage -> Products(
                name = name,
                cost = cost,
                description = binding.etDescription.text.toString(),
                size = null,
                imageid = 0,
                brandid = brandId,
                categoryid = categoryId,
                amount = amount,
                imageuri = selectedImage.url // Сохраняем URL сервера
            )
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

    private fun isValidPrice(price: String): Boolean = price.toDoubleOrNull()?.let { it > 0 } ?: false

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
        binding.btnArrowBack.setOnClickListener { finish() }
    }

    private fun setupSpinners() {
        brandAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.brandSpinner.adapter = brandAdapter

        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

        lifecycleScope.launch {
            try {
                val brands = productApi.getBrands()
                withContext(Dispatchers.Main) {
                    brandAdapter.clear()
                    brandAdapter.addAll(brands.map { it.namebrand })
                }
                val categories = productApi.getCategories()
                withContext(Dispatchers.Main) {
                    categoryAdapter.clear()
                    categoryAdapter.addAll(categories.map { it.namecategory })
                }
            } catch (e: Exception) {
                Log.e("SpinnerSetup", "Error loading brands/categories", e)
            }
        }
    }
}
