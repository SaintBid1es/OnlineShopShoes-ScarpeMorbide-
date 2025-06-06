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
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UpdateProductActivity : BaseActivity() {
    private lateinit var binding: ActivityUpdateProductBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var brandAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var selectedImagePosition = RecyclerView.NO_POSITION

    private val productApi = RetrofitClient.apiService
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
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Сохраняем изображение локально в filesDir/product_images
                val file = saveImageToInternalStorage(uri, getFileNameFromUri(uri))

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = productApi.uploadImage(multipartBody)

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateProductActivity, "Ошибка загрузки: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val imageUrl = response.body()?.url
                if (imageUrl != null) {
                    imageList.add(ProductImage.UrlImage(imageUrl))
                    selectedImagePosition = imageList.size - 1

                    withContext(Dispatchers.Main) {
                        (binding.rcViewImage.adapter as? ProductCreateAdapter)?.apply {
                            notifyItemInserted(imageList.size - 1)
                            setSelectedPosition(selectedImagePosition)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ImageUpload", "Ошибка загрузки изображения", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UpdateProductActivity, "Ошибка загрузки изображения", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun getFileNameFromUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }

        }
        return result
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

            val fileName = originalName?.takeIf { it.isNotBlank() }?.let {
                if (it.contains('.')) it else "$it.jpg"
            } ?: "img_${System.currentTimeMillis()}.jpg"

            val outputFile = File(imagesDir, fileName)

            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                } ?: throw IOException("Не удалось открыть исходное изображение")

                if (!outputFile.exists() || outputFile.length() == 0L) {
                    if (outputFile.exists()) outputFile.delete()
                    throw IOException("Файл не был создан или пуст")
                }

                return@withContext outputFile
            } catch (e: Exception) {
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
        lifecycleScope.launch {
           val brands =  productApi.getBrands()
                brandAdapter.clear()
                brandAdapter.addAll(brands.map { it.namebrand })


            val categories= productApi.getCategories()
                categoryAdapter.clear()
                categoryAdapter.addAll(categories.map { it.namecategory })

        }
    }

    private fun setupButtons() {
        binding.btnArrowBack.setOnClickListener { finish() }
    }

    private fun loadProductData(productId: Int) {
        lifecycleScope.launch {
            withAuthToken { token ->
                try {
                    val product = productApi.getProductsByID(productId, token)
                    with(binding) {
                        etNameProduct.setText(product.name)
                        etDescription.setText(product.description)
                        etCost.setText(product.cost.toString())
                        etAmount.setText(product.amount.toString())
                    }

                    // Загрузка изображения продукта
                    loadProductImage(product)

                    // Установка выбранных значений в спиннерах
                    setSpinnerSelections(product.brandid, product.categoryid)
                } catch (e: Exception) {
                    Log.e("UpdateProduct", "Error loading product", e)
                    Toast.makeText(
                        this@UpdateProductActivity,
                        "Ошибка загрузки продукта", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun loadProductImage(product: Products) {
        val imageUri = product.imageuri
        if (!imageUri.isNullOrEmpty()) {
            val imageUrl = imageUri // замените на ваш путь
            imageList.add(ProductImage.UrlImage(imageUrl))
            selectedImagePosition = imageList.size - 1
        } else if (product.imageid != 0) {
            updateSelectedImage(product.imageid)
        } else {
            imageList.add(ProductImage.DrawableImage(R.drawable.star_ic))
            selectedImagePosition = imageList.size - 1
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
            productApi.getBrandsByID(brandId)?.namebrand?.let { brandName ->
                val position = brandAdapter.getPosition(brandName)
                if (position >= 0) binding.brandSpinner.setSelection(position)
            }

            productApi.getCategoriesByID(categoryId)?.namecategory?.let { categoryName ->
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
                    viewModel.updateProduct(updatedProduct.id!!,updatedProduct)
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

        val brandId = productApi.getBrandByName(brandName)?.id
            ?: throw IllegalArgumentException("Brand '$brandName' not found")
        val categoryId = productApi.getCategoryByName(categoryName)?.id
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
                imageid = selectedImage.resId,
                brandid = brandId,
                categoryid = categoryId,
                amount = amount,
                imageuri = null
            )
            is ProductImage.UrlImage -> {
                val fileName = selectedImage.url.substringAfterLast('/')
                Products(
                    id = productId,
                    name = name,
                    cost = cost,
                    description = binding.etDescription.text.toString(),
                    size = null,
                    imageid = 0,
                    brandid = brandId,
                    categoryid = categoryId,
                    amount = amount,
                    imageuri = fileName
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