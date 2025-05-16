package com.example.testbundle.Activity.User

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.Admin.*
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.Adapter.ProductCardUserAdapter
import com.example.testbundle.FavoritePreferences
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityListProductBinding
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.launch

class ListProductActivity : BaseActivity() {
    private lateinit var binding: ActivityListProductBinding
    private lateinit var prefs: DataStore<androidx.datastore.preferences.core.Preferences>
    private lateinit var favoritePreferences: FavoritePreferences
    private lateinit var adapterBrand: ArrayAdapter<String>
    private lateinit var adapterCategory: ArrayAdapter<String>
    private lateinit var adapter: ProductCardUserAdapter

    private var recyclerViewState: Parcelable? = null

    companion object {
        var idUser: Int = 0
    }

    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupAdapters()
        setupObservers()
        setupListeners()
        restoreRecyclerState(savedInstanceState)
    }

    private fun initViews() {
        prefs = applicationContext.dataStore
        favoritePreferences = FavoritePreferences(this)
        binding.rcView.layoutManager = GridLayoutManager(this, 2)

        adapter = ProductCardUserAdapter(
            emptyList(),
            onItemClick = { product ->
                startActivity(Intent(this, DetailProductActivity::class.java).apply {
                    putExtra("product_id", product.id)
                })
            },
            onFavoriteClick = { productId ->
                viewModel.toggleFavorite(productId)
            }
        )
        binding.rcView.adapter = adapter
    }

    private fun setupAdapters() {
        val db = MainDb.getDb(this)
        adapterBrand = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapterBrand.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterCategory = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.SpinnerBrand.adapter = adapterBrand
        binding.SpinnerCategory.adapter = adapterCategory

        db.getDao().getAllCategory().asLiveData().observe(this) { categories ->
            adapterCategory.clear()
            adapterCategory.add(getString(R.string.all))
            adapterCategory.addAll(categories.map { it.name })
        }

        db.getDao().getAllBrand().asLiveData().observe(this) { brands ->
            if (brands.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.No_brand_data_found), Toast.LENGTH_SHORT).show()
            } else {
                adapterBrand.clear()
                adapterBrand.add(getString(R.string.all))
                adapterBrand.addAll(brands.map { it.name })
                binding.SpinnerBrand.isEnabled = true
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect { preferences ->
                    loadUserData(
                        preferences[ProfileActivity.EMAIL_KEY],
                        preferences[ProfileActivity.PASSWORD_KEY]
                    )
                    SearchUserId(
                        preferences[ProfileActivity.EMAIL_KEY],
                        preferences[ProfileActivity.PASSWORD_KEY]
                    )
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateProduct.collect { products ->
                    updateProductList(products)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.SpinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                viewModel.setCategoryFilter(adapterCategory.getItem(position))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.SpinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                viewModel.setBrandFilter(adapterBrand.getItem(position))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.btnSearch.setOnClickListener {
            filterProducts(binding.etSearch.text.toString().trim())
        }

        /**
         * Навигационное меню
         */
        binding.imgFavorite.setOnClickListener { startActivity(Intent(this, FavoriteActivity::class.java)) }
        binding.imgBasket.setOnClickListener { startActivity(Intent(this, BasketActivity::class.java)) }
        binding.imgProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.imgClients.setOnClickListener { startActivity(Intent(this, ListEmployeeActivity::class.java)) }
        binding.imgProduct.setOnClickListener { startActivity(Intent(this, ListProductAdminActivity::class.java)) }
        binding.imgOrderHistory.setOnClickListener { startActivity(Intent(this, OrderHistoryActivity::class.java)) }
        binding.imgCreateBrand.setOnClickListener { startActivity(Intent(this, BrandAndCategoryViewActivity::class.java)) }
        binding.imgCreateCategory.setOnClickListener { startActivity(Intent(this, BrandAndCategoryViewActivity::class.java)) }
    }

    private fun updateProductList(products: List<ProductsModel>) {
        recyclerViewState = binding.rcView.layoutManager?.onSaveInstanceState()
        adapter.updateList(products)
        recyclerViewState?.let { binding.rcView.layoutManager?.onRestoreInstanceState(it) }
    }

    /**
     * Функция фильтраци
     */
    private fun filterProducts(searchText: String) {
        val filteredList = if (searchText.isEmpty()) {
            viewModel.stateProduct.value
        } else {
            viewModel.stateProduct.value?.filter { product ->
                product.name.contains(searchText, ignoreCase = true)
            }
        }
        filteredList?.let { updateProductList(it) }
    }

    private fun loadUserData(email: String?, password: String?) {
        MainDb.getDb(this).getDao().getAllItems().asLiveData().observe(this) { list ->
            list.find { it.email == email && it.password == password }?.let { user ->
                binding.tvNameAccount.text = user.Name
                val isAdmin = user.speciality == "Администратор" || user.speciality == "Administrator"
                binding.layoutProduct.isVisible = isAdmin
                binding.layoutUsers.isVisible = isAdmin

            }
        }
    }

    private fun SearchUserId(email: String?, password: String?) {
        MainDb.getDb(this).getDao().getAllItems().asLiveData().observe(this) { list ->
            list.find { it.email == email && it.password == password }?.let {
                idUser = it.id ?: 0
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("recycler_state", binding.rcView.layoutManager?.onSaveInstanceState())
    }

    private fun restoreRecyclerState(savedInstanceState: Bundle?) {
        savedInstanceState?.getParcelable<Parcelable>("recycler_state")?.let {
            recyclerViewState = it
        }
    }
}