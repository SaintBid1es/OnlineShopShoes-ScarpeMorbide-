package com.example.testbundle.Activity.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoesonlineshop.activity.BaseActivity


import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Activity.MainActivity
import com.example.testbundle.Activity.User.BasketActivity
import com.example.testbundle.Activity.User.FavoriteActivity
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.Activity.User.ListProductActivity.Companion.idUser
import com.example.testbundle.Activity.User.OrderHistoryActivity
import com.example.testbundle.Activity.User.ProfileActivity
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.Adapter.AccountCardAdapter
import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityListEmployeeBinding
import com.example.testbundle.db.Brand

import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class ListEmployeeActivity : BaseActivity() {
    private lateinit var binding: ActivityListEmployeeBinding
    lateinit var prefs: DataStore<androidx.datastore.preferences.core.Preferences>

    private var currentUserId:Int?=-1


    val viewModel : MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListEmployeeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = applicationContext.dataStore

        binding.rcView.layoutManager = LinearLayoutManager(this)
        /**
         * Навигационная панель
         */
        binding.imgFavorite.setOnClickListener {
            startActivity(Intent(this@ListEmployeeActivity, FavoriteActivity::class.java))
        }
        binding.imgBasket.setOnClickListener {
            startActivity(Intent(this@ListEmployeeActivity, BasketActivity::class.java))
        }
        binding.imgProfile.setOnClickListener {
            startActivity(Intent(this@ListEmployeeActivity, ProfileActivity::class.java))
        }
        binding.imgMain.setOnClickListener {
            startActivity(Intent(this@ListEmployeeActivity, ListProductActivity::class.java))
        }

        binding.imgProduct.setOnClickListener {
            startActivity(Intent(this@ListEmployeeActivity,ListProductAdminActivity::class.java))
        }
        binding.imgOrderHistory.setOnClickListener {
            startActivity(Intent(this@ListEmployeeActivity, OrderHistoryActivity::class.java))
        }
        binding.btnAddUser.setOnClickListener {
            startActivity(Intent(this@ListEmployeeActivity, CreateUserActivity::class.java))
        }
        /**
         * Настройка поиска
         */
        setupSearch()
        /**
         * Поиск ID пользователя
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect {
                    currentUserId = it[DataStoreRepo.USER_ID_KEY]
                }
            }
        }
        /**
         * Вывод списка recyclerView
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel?.state?.collect{
                    onUpdateView(it)

                }
            }
        }






    }

    private fun onUpdateView(entities: List<Item>) {
        binding.apply {
            val filteredEntities = entities.filter { it.id != currentUserId }
            rcView.adapter = AccountCardAdapter(
                filteredEntities,
                onEdit = {
                    intent = Intent(this@ListEmployeeActivity, EditAccountCardActivity::class.java).apply {
                        putExtra("item_id", it.id)
                    }
                    startActivity(intent)
                },
                onDelete = { id ->
                    viewModel?.deleteItem(id)
                }
            )
        }
    }

    /**
     * Реализация поиска товара по названию
     */
    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            val searchText = binding.etSearch.text.toString().trim()
            filterProducts(searchText)
        }
    }
    /**
     * Фильтрация товаров по категории
     * @param searchText[String]
     */
    private fun filterProducts(searchText: String) {
        val filteredList = if (searchText.isEmpty()) {
            viewModel.state.value
        } else {
            viewModel.state.value?.filter { user ->
                user.email.contains(searchText, ignoreCase = true)
            }
        }
        onUpdateView(filteredList ?: emptyList())
    }

}