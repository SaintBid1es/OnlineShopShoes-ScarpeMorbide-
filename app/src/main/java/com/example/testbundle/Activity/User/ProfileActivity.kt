package com.example.testbundle.Activity.User

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.Visibility
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.Admin.ListProductAdminActivity
import com.example.testbundle.Activity.Analyst.GraphicActivity
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
import com.example.testbundle.databinding.ActivityProfileBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.util.prefs.Preferences

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    val viewModel: ProductViewModel by viewModels()
    val viewModelBrand: BrandViewModel by viewModels()
    val viewModelCategory: CategoryViewModel by viewModels()
    val viewModelClient: MainViewModel by viewModels()
    private lateinit var Client: Item
    lateinit var prefs : DataStore<androidx.datastore.preferences.core.Preferences>
    companion object {
        val EMAIL_KEY = stringPreferencesKey("email")
        val PASSWORD_KEY = stringPreferencesKey("password")
        var idAccount = 0
        var language = false
    }
    val MY_REQUEST_CODE1 = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        val db = MainDb.getDb(this)
        super.onCreate(savedInstanceState)
        prefs = applicationContext.dataStore
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

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


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect {
                    val userId = it[USER_ID_KEY]
                    userId?.let { loadDataById(it) }
                }
            }
        }
        /**
         * Смена языка
         */
        binding.btnLanguage.setOnClickListener {
                changeLanguage()
        }
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
                val user = db.getDao().getAccountById(idAccount)
                val updatedUser = user.copy(avatar = null)
                viewModelClient.updateItem(updatedUser)
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

                show_URI_in_ImageView(uri)//content://com.android.providers.media.documents/document/image%3A1000038190
                val UpdateClient = Client.copy(avatar = uri.toString())
                viewModelClient.updateItem(UpdateClient)
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
        val db = MainDb.getDb(this)
        lifecycleScope.launch {
            val user = db.getDao().getAccountById(userId)
            user?.let {
                with(binding) {
                    tvLogin.text = it.email
                    tvPassword.text = it.password
                    tvName.text = it.Name
                    tvSurName.text = it.SurName
                    tvTelephone.text = it.telephone
                    tvSpeciality.text = it.speciality
                    if (!it.avatar.isNullOrEmpty()){
                        ivAvatarProfile.setImageURI(it.avatar!!.toUri())

                    }else {
                        ivAvatarProfile.setImageResource(R.drawable.avatarmen)
                    }
                    idAccount = it.id!!
                    Client = Item(it.id,it.password,it.Name,it.SurName,it.email,it.telephone,it.speciality,null)
                }
                if (it.speciality == "Администратор" || it.speciality == "Administrator" ) {
                    binding.layoutUsers.isVisible = true
                    binding.layoutProduct.isVisible = true
                }
                if (it.speciality == "Аналитик" || it.speciality == "Analyst" ) {
                    binding.layoutMain.isVisible = false
                    binding.layoutBasket.isVisible = false
                    binding.layoutFavorite.isVisible = false
                    binding.layoutHistory.isVisible = false
                    binding.layoutGraphic.isVisible = true
                }
            }
        }
    }

    /**
     * Инициализация брэндов и категорий
     */
   suspend  fun initBrandandCategory() {
        val db = MainDb.getDb(this)
        val categories = db.getDao().getAllCategory().first()
        val brands = db.getDao().getAllBrand().first()
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
    private fun changeLanguage() {
        val languageCode = if (!language) "ru" else "en"
        LocaleUtils.setLocale(this, languageCode)
        language = !language
        recreate()
    }

}