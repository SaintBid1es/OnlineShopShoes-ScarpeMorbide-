package com.example.testbundle.Activity.User

import OrderHistoryAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.Admin.ListProductAdminActivity
import com.example.testbundle.Activity.User.ListProductActivity.Companion.idUser
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.OrderViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityOrderHistoryBinding
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Order
import com.example.testbundle.db.OrderItem
import com.example.testbundle.db.OrderModel
import kotlinx.coroutines.launch
import java.util.UUID

class OrderHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var prefs: DataStore<androidx.datastore.preferences.core.Preferences>
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = applicationContext.dataStore

        val recyclerView: RecyclerView = binding.rcViewOrderHistory
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        orderHistoryAdapter = OrderHistoryAdapter(emptyList())
        recyclerView.adapter = orderHistoryAdapter



        setupClickListeners()
        /**
         * Загрузка данных пользователя
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect {
                    loadData(it[ProfileActivity.EMAIL_KEY], it[ProfileActivity.PASSWORD_KEY])
                }
            }
        }
        /**
         * Функция очистки истории заказов
         */
        binding.btnTrashBack.setOnClickListener {
            viewModel.deleteOrder()
            Toast.makeText(
                this@OrderHistoryActivity,
                getString(R.string.order_history_is_cleaner),
                Toast.LENGTH_SHORT
            ).show()
        }
        /**
         * Вывод истории заказов пользователя
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderItems.collect { order ->
                    onUpdateView(order)
                }
            }
        }
    }


    /**
     * Функция загрузки данных пользователя
     * @param email[String] password[String]
     */
    private fun loadData(email: String?, password: String?) {
        val db = MainDb.getDb(this)
        db.getDao().getAllItems().asLiveData().observe(this) { list ->
            val user = list.find { it.email == email && it.password == password }
            user?.let {
                if (it.speciality == "Администратор" || it.speciality == "Administrator" ) {
                    binding.layoutProduct.isVisible = true
                    binding.layoutUsers.isVisible = true
                }
            }
        }
    }

    /**
     * Навигационное меню
     */
    private fun setupClickListeners() {
        binding.imgFavorite.setOnClickListener {
            startActivity(Intent(this@OrderHistoryActivity, FavoriteActivity::class.java))
        }
        binding.imgBasket.setOnClickListener {
            startActivity(Intent(this@OrderHistoryActivity, BasketActivity::class.java))
        }
        binding.imgProfile.setOnClickListener {
            startActivity(Intent(this@OrderHistoryActivity, ProfileActivity::class.java))
        }
        binding.imgMain.setOnClickListener {
            startActivity(Intent(this@OrderHistoryActivity, ListProductActivity::class.java))
        }
        binding.imgClients.setOnClickListener {
            startActivity(Intent(this@OrderHistoryActivity, ListEmployeeActivity::class.java))
        }
        binding.imgProduct.setOnClickListener {
            startActivity(Intent(this@OrderHistoryActivity, ListProductAdminActivity::class.java))
        }
    }

    private fun onUpdateView(orders: List<OrderModel>) {
        binding.apply {
            rcViewOrderHistory.adapter = OrderHistoryAdapter(orders)
           // orderHistoryAdapter.updateData(orders)
        }
    }
}