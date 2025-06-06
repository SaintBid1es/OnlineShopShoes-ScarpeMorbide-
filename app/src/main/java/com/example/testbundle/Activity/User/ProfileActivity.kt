package com.example.testbundle.Activity.User

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.*
import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.Admin.ListProductAdminActivity
import com.example.testbundle.Activity.Analyst.GraphicActivity
import com.example.testbundle.BrandViewModel
import com.example.testbundle.CategoryViewModel
import com.example.testbundle.LocaleUtils
import com.example.testbundle.MainViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.Repository.AuthRepository
import com.example.testbundle.databinding.ActivityProfileBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import com.example.testbundle.db.Item
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import org.json.JSONObject
import java.io.File
import kotlin.io.path.createTempFile

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    lateinit var prefs: DataStore<androidx.datastore.preferences.core.Preferences>
    private lateinit var authRepository: AuthRepository
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var Client: Item
    val viewModel: ProductViewModel by viewModels()
    val viewModelBrand: BrandViewModel by viewModels()
    val viewModelCategory: CategoryViewModel by viewModels()
    val viewModelClient: MainViewModel by viewModels()

    companion object {
        val EMAIL_KEY = androidx.datastore.preferences.core.stringPreferencesKey("email")
        val PASSWORD_KEY = androidx.datastore.preferences.core.stringPreferencesKey("password")
        var idAccount = 0
    }

    private val productApi = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = applicationContext.dataStore
        authRepository = AuthRepository(applicationContext)

        initImagePicker()

        lifecycleScope.launch { initBrandandCategory() }

        setupNavigation()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                prefs.data.collect { preferences ->
                    preferences[DataStoreRepo.LANGUAGE_KEY]?.let { updateLocale(it) }

            withAuthToken {token->
            val json = decodeJwtToken(token)?.utf8()
                val jsonObject  = JSONObject(json)
                val userId = jsonObject.getString("userId").toString().toInt()
              //  {"sub":"111@gmail.com","jti":"a7f46fc8-ad45-4a542-8be3-163d81b84f84","userId":"1","http://schemas.microsoft.com/ws/2008/06/identity/claims/role":"Administrator","exp":1749062230,"iss":"your_api_domain.com","aud":"your_client_app"} функция возвращает это как мне получить userId

                    if (userId != null) {
                        loadDataById(userId)
                    } else {
                        Log.e("ProfileActivity", "User ID not found in DataStore")
                    }
            }


                }
            }

        }


        binding.btnImageAddPhotoProfile.setOnClickListener {
            imagePickerLauncher.launch(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
            )
        }

        binding.btnImageResetPhotoProfile.setOnClickListener {
            binding.ivAvatarProfile.setImageResource(R.drawable.avatarmen)
            lifecycleScope.launch {
                withAuthToken { token ->
                    val user = productApi.getUsersByID(idAccount, token)
                    val updatedUser = user.copy(avatar = null)
                    viewModelClient.updateItem(user.id!!, updatedUser)
                }
            }
        }

        setupLanguageToggle()
    }

    private fun initImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult

                binding.ivAvatarProfile.load(uri) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.avatarmen)
                    error(R.drawable.avatarmen)
                }

                lifecycleScope.launch {
                    if (!::Client.isInitialized) {
                        Log.e("ProfileActivity", "Client not initialized yet")
                        return@launch
                    }

                    val uploadedUrl = uploadImageFromUri(uri)

                    if (uploadedUrl != null) {
                        val uniqueUrl = "$uploadedUrl?t=${System.currentTimeMillis()}"
                        val updatedClient = Client.copy(avatar = uniqueUrl)
                        viewModelClient.updateItem(updatedClient.id!!, updatedClient)
                        Client = updatedClient

                        val request = ImageRequest.Builder(this@ProfileActivity)
                            .data(uniqueUrl)
                            .target { drawable ->
                                binding.ivAvatarProfile.setImageDrawable(drawable)
                            }
                            .placeholder(R.drawable.avatarmen)
                            .error(R.drawable.avatarmen)
                            .transformations(CircleCropTransformation())
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build()

                        ImageLoader(this@ProfileActivity).enqueue(request)
                    } else {
                        Log.e("ProfileActivity", "Failed to upload avatar image")
                        binding.ivAvatarProfile.setImageResource(R.drawable.avatarmen)
                    }
                }
            }
        }
    }

    private fun setupNavigation() {
        binding.imgFavorite.setOnClickListener { startActivity(Intent(this, FavoriteActivity::class.java)) }
        binding.imgBasket.setOnClickListener { startActivity(Intent(this, BasketActivity::class.java)) }
        binding.imgMain.setOnClickListener { startActivity(Intent(this, ListProductActivity::class.java)) }
        binding.btnArrowBack.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.imgClients.setOnClickListener { startActivity(Intent(this, ListEmployeeActivity::class.java)) }
        binding.imgProduct.setOnClickListener { startActivity(Intent(this, ListProductAdminActivity::class.java)) }
        binding.imgOrderHistory.setOnClickListener { startActivity(Intent(this, OrderHistoryActivity::class.java)) }
        binding.imgGraphic.setOnClickListener { startActivity(Intent(this, GraphicActivity::class.java)) }
        binding.btnEditInf.setOnClickListener {
            val intent = Intent(this, UpdateInformationActivity::class.java)
            intent.putExtra("item_id", idAccount)
            startActivity(intent)
        }
    }

    private suspend fun loadDataById(userId: Int) {
        withAuthToken { token ->
            val user = productApi.getUsersByID(userId, token)
            user?.let {
                with(binding) {
                    tvLogin.text = it.email
                    tvPassword.text = it.password
                    tvName.text = it.name
                    tvSurName.text = it.surname
                    tvTelephone.text = it.telephone
                    tvSpeciality.text = it.speciality

                    if (!it.avatar.isNullOrEmpty()) {
                        ivAvatarProfile.load(it.avatar) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                            placeholder(R.drawable.avatarmen)
                            error(R.drawable.avatarmen)
                        }
                    } else {
                        ivAvatarProfile.setImageResource(R.drawable.avatarmen)
                    }

                    idAccount = it.id!!
                    Client = Item(
                        it.id, it.password, it.name, it.surname,
                        it.email, it.telephone, it.speciality, it.avatar
                    )
                }

                if (it.speciality == "Администратор" || it.speciality == "Administrator") {
                    binding.layoutUsers.isVisible = true
                    binding.layoutProduct.isVisible = true
                }
                if (it.speciality == "Аналитик" || it.speciality == "Analyst") {
                    binding.layoutMain.isVisible = false
                    binding.layoutBasket.isVisible = false
                    binding.layoutFavorite.isVisible = false
                    binding.layoutHistory.isVisible = false
                    binding.layoutGraphic.isVisible = true
                }
            }
        }
    }

    private suspend fun uploadImageFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val tempFile = createTempFile(suffix = ".jpg").toFile()
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            val response = productApi.uploadImage(body)
            if (response.isSuccessful) response.body()?.url else null
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Upload exception", e)
            null
        }
    }

    private suspend fun initBrandandCategory() {
        val categories = productApi.getCategories()
        val brands = productApi.getBrands()
        if (brands.isEmpty()) {
            viewModelBrand.insertBrand(Brand(1, "Nike"))
            viewModelBrand.insertBrand(Brand(2, "Puma"))
            viewModelBrand.insertBrand(Brand(3, "Adidas"))
        }
        if (categories.isEmpty()) {
            viewModelCategory.insertCategory(Category(1, "Summer"))
            viewModelCategory.insertCategory(Category(2, "Winter"))
        }
    }

    private suspend fun updateLocale(language: Boolean) {
        val languageCode = if (language) "ru" else "en"
        LocaleUtils.setLocale(this@ProfileActivity, languageCode)
    }

    private fun setupLanguageToggle() {
        binding.btnLanguage.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                prefs.edit { preferences ->
                    val currentLanguage = preferences[DataStoreRepo.LANGUAGE_KEY] ?: false
                    preferences[DataStoreRepo.LANGUAGE_KEY] = !currentLanguage
                }
                withContext(Dispatchers.Main) { recreate() }
            }
        }
    }
    fun decodeJwtToken(token: String): ByteString? {
        // Проверяем, что токен имеет правильный формат (три части, разделенные точками)
        val parts = token.split(".")
        if (parts.size != 3) {
            return null
        }

        // Декодируем заголовок и полезную нагрузку
        val headerBase64 = parts[0]
        val payloadBase64 = parts[1]

        // Конвертируем Base64 строки в JSON объекты
        val headerJson: ByteString? = headerBase64.decodeBase64()
        val payloadJson: ByteString? = payloadBase64.decodeBase64()

        // Возвращаем полезную нагрузку (payload)
        return payloadJson
    }
}
