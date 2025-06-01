package com.example.testbundle.Activity.User

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.Visibility
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.Admin.ListProductAdminActivity
import com.example.testbundle.Activity.Analyst.GraphicActivity
import com.example.testbundle.Activity.DataStoreRepo.Companion.LANGUAGE_KEY
import com.example.testbundle.Activity.DataStoreRepo.Companion.USER_ID_KEY
import com.example.testbundle.Activity.MainActivity
import com.example.testbundle.Activity.User.ListProductActivity.Companion.idUser
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.BrandViewModel
import com.example.testbundle.CategoryViewModel
import com.example.testbundle.LocaleUtils
import com.example.testbundle.MainViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.Repository.AuthRepository
import com.example.testbundle.Repository.Result
import com.example.testbundle.databinding.ActivityProfileBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.prefs.Preferences

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    val viewModel: ProductViewModel by viewModels()
    val viewModelBrand: BrandViewModel by viewModels()
    val viewModelCategory: CategoryViewModel by viewModels()
    val viewModelClient: MainViewModel by viewModels()
    private lateinit var authRepository: AuthRepository
    private lateinit var Client: Item
    lateinit var prefs : DataStore<androidx.datastore.preferences.core.Preferences>
    companion object {
        val EMAIL_KEY = stringPreferencesKey("email")
        val PASSWORD_KEY = stringPreferencesKey("password")
        var idAccount = 0

    }
    val MY_REQUEST_CODE1 = 100
    private val productApi = RetrofitClient.apiService
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        prefs = applicationContext.dataStore
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authRepository = AuthRepository(applicationContext)

        /**
         * Инцициализация  категорий и брэндов
         */
        lifecycleScope.launch {
            initBrandandCategory()
        }
        /**
         * Навигационное меню
         */
        binding.imgFavorite.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, FavoriteActivity::class.java))
        }
        binding.imgBasket.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, BasketActivity::class.java))
        }

        binding.imgMain.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ListProductActivity::class.java))
        }

        binding.btnArrowBack.setOnClickListener {
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            authRepository.logout()
            startActivity(intent)
        }
        binding.imgClients.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ListEmployeeActivity::class.java))
        }
        binding.imgProduct.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ListProductAdminActivity::class.java))
        }
        binding.imgOrderHistory.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, OrderHistoryActivity::class.java))
        }
        binding.btnEditInf.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, UpdateInformationActivity::class.java))
            intent.putExtra("item_id", idAccount)
        }
        binding.imgGraphic.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, GraphicActivity::class.java))
        }
        setupLanguageToggle()


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                prefs.data.collect { preferences ->
                    val userId = preferences[USER_ID_KEY]
                    if (userId != null) {
                        loadDataById(userId)
                    } else {
                        // Обработка случая, когда userId отсутствует
                        Log.e("ProfileActivity", "User ID not found in DataStore")
                    }
                    preferences[LANGUAGE_KEY]?.let { updateLocale(it) }
                }
            }
        }

        /**
         * Смена языка
         */
//        binding.btnLanguage.setOnClickListener {
//            CoroutineScope(Dispatchers.IO).launch {
//                changeLanguage()
//            }
//        }
        binding.btnImageAddPhotoProfile.setOnClickListener {
            val intent = Intent(ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, MY_REQUEST_CODE1)
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


    }



    // I override system function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == MY_REQUEST_CODE1) {
            data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                show_URI_in_ImageView(uri)
                val UpdateClient = Client.copy(avatar = uri.toString())
                viewModelClient.updateItem(UpdateClient.id!!,UpdateClient)
            }
        }
    }

    // my function for image view
    fun show_URI_in_ImageView(uri: Uri?)
    {
        val myImageView = findViewById(R.id.ivAvatarProfile) as ImageView
        myImageView.setImageURI(uri)

    }

    /**
     * Загрузка данных
     */
    private fun loadDataById(userId: Int) {

        lifecycleScope.launch {
            withAuthToken { token->
                val user = productApi.getUsersByID(userId,  token)
                user?.let {
                    with(binding) {
                        tvLogin.text = it.email
                        tvPassword.text = it.password
                        tvName.text = it.name
                        tvSurName.text = it.surname
                        tvTelephone.text = it.telephone
                        tvSpeciality.text = it.speciality
                        if (!it.avatar.isNullOrEmpty()) {
                            ivAvatarProfile.setImageURI(it.avatar!!.toUri())
                        } else {
                            ivAvatarProfile.setImageResource(R.drawable.avatarmen)
                        }
                        idAccount = it.id!!
                        Client = Item(
                            it.id,
                            it.password,
                            it.name,
                            it.surname,
                            it.email,
                            it.telephone,
                            it.speciality,
                            null
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
    }

    /**
     * Инициализация брэндов и категорий
     */
   suspend  fun initBrandandCategory() {

        val categories = productApi.getCategories()
        val brands = productApi.getBrands()
        if ( brands.isEmpty()) {
                val Brand = Brand(1, "Nike")
                val Brand1 = Brand(2, "Puma")
                val Brand2 = Brand(3, "Adidas")
                viewModelBrand.insertBrand(Brand)
                viewModelBrand.insertBrand(Brand1)
                viewModelBrand.insertBrand(Brand2)
        }
        if (categories.isEmpty()) {
            val Category = Category(1, "Summer")
            val Category1 = Category(2, "Winter")
            viewModelCategory.insertCategory(Category)
            viewModelCategory.insertCategory(Category1)
        }
    }

    /**
     * Функция смена языка
     */
    private suspend fun updateLocale(language: Boolean) {
        val languageCode = if (language) "ru" else "en"
        LocaleUtils.setLocale(this@ProfileActivity, languageCode)
    }
    private fun setupLanguageToggle() {
        binding.btnLanguage.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                prefs.edit { preferences ->
                    val currentLanguage = preferences[LANGUAGE_KEY] ?: false
                    preferences[LANGUAGE_KEY] = !currentLanguage
                }
                withContext(Dispatchers.Main) { recreate() }
            }
        }
    }

}