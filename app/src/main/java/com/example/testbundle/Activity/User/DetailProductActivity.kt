package com.example.testbundle.Activity.User

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.Adapter.SizeAdapter
import com.example.testbundle.BasketViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.Repository.AuthRepository
import com.example.testbundle.databinding.ActivityDetailProductBinding
import com.example.testbundle.db.Basket
import com.example.testbundle.db.MainDb
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailProductActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailProductBinding

    private lateinit var prefs: DataStore<androidx.datastore.preferences.core.Preferences>
    private var sizeAdapter: SizeAdapter? = null
    private var selectedSize: Int = -1
    private var productId: Int = 0
    private val viewModelBasket: BasketViewModel by viewModels()
    private val productApi = RetrofitClient.apiService
    companion object {
        var idUser: Int = 0
        private const val SIZE_PREFIX = "size_product_"
        private const val SELECTED_SIZE_KEY = "SELECTED_SIZE"
    }
    private lateinit var authRepository: AuthRepository

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = applicationContext.dataStore
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(applicationContext)
        selectedSize = savedInstanceState?.getInt(SELECTED_SIZE_KEY) ?: -1

        selectedSize = intent.getIntExtra("size_id", -1).takeIf { it != -1 }
            ?: savedInstanceState?.getInt(SELECTED_SIZE_KEY, -1)
                    ?: -1
        initSizeList()
        initUser()

        loadProductDetails()
        setupObservers()
        setupClickListeners()
        binding.tvCreateReviews.setOnClickListener {
            val i = Intent(this, CreateReviewActivity::class.java)
            i.putExtra("product_id",productId)
            startActivity(i)
        }
        binding.tvCountReviews.setOnClickListener {
            val i = Intent(this, ReviewsActivity::class.java)
            i.putExtra("product_id",productId)
            startActivity(i)
        }
    }

    /**
     * Инициализация пользователя,который в системе по почте и паролю
     */
    private fun initUser() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect {
                    SearchUserId(
                        it[ProfileActivity.EMAIL_KEY],
                        it[ProfileActivity.PASSWORD_KEY]
                    )
                }
            }
        }

    }
    /**
     * Инициализация пользователя,который в системе по почте и паролю
     * @param email[String] password[String]
     */
    private fun SearchUserId(email: String?, password: String?) {
        lifecycleScope.launch {
            withAuthToken {token->
            try {
                    val list = productApi.getUsers(token)
                    val user = list.find { it.email == email && it.password == password }

                    user?.let {
                        idUser = it.id ?: throw IllegalStateException("User ID is null")
                    } ?: run {
                        // Обработка случая, когда пользователь не найден
                    }
                } catch (e: Exception) {
                    // Обработка ошибок сети и других исключений
                }

            }
        }
    }

    /**
     * Подгрузка данных товара
     */
    private fun loadProductDetails() {
        productId = intent.getIntExtra("product_id", 0)
        lifecycleScope.launch {

            if (selectedSize == -1) {
                selectedSize = prefs.data.first()[intPreferencesKey("$SIZE_PREFIX$productId")] ?: -1
            }
            if (selectedSize != -1) {
                sizeAdapter?.setSelectedPosition(selectedSize)
                checkProductInBasket(selectedSize)
            }
            val product = productApi.getProductsByID(productId) ?: return@launch
            binding.tvNameProduct.text = product.name
            binding.tvDescriptionProduct.text = product.description
            binding.tvCostProduct.text = "${product.cost}"
            if (!product.imageuri.isNullOrEmpty()) {
                try {
                    // Check if the URI is already complete or just a filename
                    val imageUri = if (product.imageuri.startsWith("content://")) {
                        Uri.parse(product.imageuri)
                    } else {
                        // Construct proper URI for the image file
                        Uri.parse("content://com.example.testbundle.fileprovider/product_images/${product.imageuri}")
                    }
                    runOnUiThread {
                        Glide.with(this@DetailProductActivity)
                            .load(imageUri)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.avatarmen)
                            .into(binding.imgShoes)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        binding.imgShoes.setImageResource(R.drawable.avatarmen)
                    }
                }
            } else {
                runOnUiThread {
                    binding.imgShoes.setImageResource(product.imageid ?: R.drawable.avatarmen)
                }
            }

//            binding.imgShoes.setImageResource(product.imageId)
            runOnUiThread {
                binding.tvAmountProduct.text = product.amount.toString()
            }
            val rating = viewModelBasket.calculateTotalRating(productId)
            if (rating.isNaN()){
                binding.tvRating.text = "0.0"
            }else{
                binding.tvRating.text = "${String.format("%.2f", rating)}"
            }
            binding.tvCountReviews.text = viewModelBasket.countProductById(productId).toString()
            lifecycleScope.launch {
                val brand = productApi.getBrandNameById(product.brandid)
                    binding.tvBrand.text = brand

              val category =  productApi.getCategoryNameById(product.categoryid)
                    binding.tvCategory.text = category

            }
        }
    }

    /**
     * Инициализация размеров обуви
     */
    private fun initSizeList() {
        val sizeList = (6..45).map { it.toString() }.toMutableList()
        binding.sizeList.apply {
            adapter = SizeAdapter(sizeList, selectedSize) { index ->
                selectedSize = index
                saveSelectedSize()
                checkProductAvailability()
            }
            layoutManager = LinearLayoutManager(
                this@DetailProductActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    /**
     * Функция Сохранения размера
     */
    private fun saveSelectedSize() {
        lifecycleScope.launch {
            prefs.edit { preferences ->
                preferences[intPreferencesKey("$SIZE_PREFIX$productId")] = selectedSize
            }
        }
    }


    /**
     * Функция проверки выбора размера
     */
    private fun checkProductAvailability() {
        if (selectedSize != -1) {
            checkProductInBasket(selectedSize)
        }
    }

    /**
     * Функция проверки продукта в корзине
     */
    private fun checkProductInBasket(size: Int) {
        lifecycleScope.launch {
            val count = productApi.getBasketItemByProductAndSize(size, productId, idUser) ?: 0
            if (count > 0) {
                setProductInBasketUI()
            } else {
                resetProductUI()
            }
        }
    }

    /**
     * Функция изменения состояние кнопки, если продукт в корзине
     */
    private fun setProductInBasketUI() {
        binding.btnShopNow.apply {
            setBackgroundColor(getColor(R.color.grey))
            text = getString(R.string.product_in_basket)
            isClickable = false
        }
    }

    private fun resetProductUI() {
        binding.btnShopNow.apply {
            setBackgroundColor(getColor(R.color.purple))
            text = getString(R.string.add_to_basket)
            isClickable = true
        }
    }

    private fun setupObservers() {
        viewModelBasket.Messages.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnArrowBack.setOnClickListener {
            startActivity(Intent(this@DetailProductActivity, ListProductActivity::class.java))
        }

        binding.btnShopNow.setOnClickListener {
            val amountCheck:Int = binding.tvAmountProduct.text.toString().toInt()
            if (selectedSize == -1) {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_razmer),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (amountCheck<=0){
                Toast.makeText(this@DetailProductActivity,"Товара нет в наличии",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addToBasket()
        }
    }
    /**
     * Функция добавления товара в корзину
     */
    private fun addToBasket() {
        lifecycleScope.launch {
            val existingCount = productApi.getBasketItemByProductAndSize(
                selectedSize, productId, idUser
            ) ?: 0

            if (existingCount > 0) {
                Toast.makeText(
                    this@DetailProductActivity,
                    getString(R.string.product_already_in_basket),
                    Toast.LENGTH_SHORT
                ).show()
                setProductInBasketUI()
                return@launch
            }


            val basketItem = Basket(
                id = null,
                clientId = idUser,
                productId = productId,
                countbasket = 1,
                size = selectedSize
            )

            viewModelBasket.insertItem(basketItem, getString(R.string.success_add_to_basket),"ERROR")

            startActivity(Intent(this@DetailProductActivity, ListProductActivity::class.java))
        }
    }

    /**
     * Сохранение размеров
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_SIZE_KEY, selectedSize)
    }
}