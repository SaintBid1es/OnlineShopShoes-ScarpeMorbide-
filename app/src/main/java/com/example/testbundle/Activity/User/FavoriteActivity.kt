package com.example.testbundle.Activity.User

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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.Admin.EditAccountCardActivity
import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.Admin.ListProductAdminActivity
import com.example.testbundle.Activity.User.DetailProductActivity.Companion.idUser
import com.example.testbundle.Activity.User.ProfileActivity.Companion
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.Adapter.AccountCardAdapter

import com.example.testbundle.Adapter.FavoriteAdapter
import com.example.testbundle.FavoriteViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityFavoriteBinding
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.launch

class FavoriteActivity : BaseActivity() {
    lateinit var binding:ActivityFavoriteBinding
    lateinit var prefs : DataStore<androidx.datastore.preferences.core.Preferences>
    val viewModel : FavoriteViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        prefs = applicationContext.dataStore
        setContentView(binding.root)
        binding.rcViewFavorite.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateFavorite?.collect{
                    onUpdateView(it)
                }
            }
        }
        /**
         * Навигационная панель
         */
        binding.imgBasket.setOnClickListener {
            startActivity(Intent(this@FavoriteActivity, BasketActivity::class.java))
        }
        binding.imgProfile.setOnClickListener {
            startActivity(Intent(this@FavoriteActivity, ProfileActivity::class.java))
        }
        binding.imgMain.setOnClickListener {
            startActivity(Intent(this@FavoriteActivity,ListProductActivity::class.java))
        }
        binding.imgClients.setOnClickListener {
            startActivity(Intent(this@FavoriteActivity, ListEmployeeActivity::class.java))
        }
        binding.imgProduct.setOnClickListener {
            startActivity(Intent(this@FavoriteActivity, ListProductAdminActivity::class.java))
        }
        binding.imgOrderHistory.setOnClickListener {
            startActivity(Intent(this@FavoriteActivity, OrderHistoryActivity::class.java))
        }
        /**
         * Проверка роли
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED){
                prefs.data.collect{
                    CheckRole(it[ProfileActivity.EMAIL_KEY],it[ProfileActivity.PASSWORD_KEY])
                }
            }
        }

    }


    /**
     * Функция проверка роли пользователя
     */
    private fun CheckRole(email: String?, password: String?) {
        val db = MainDb.getDb(this)
        db.getDao().getAllItems().asLiveData().observe(this) { list ->
            val user = list.find { it.email == email && it.password == password }
            user?.let {
                if (it.speciality == "Администратор" || it.speciality == "Administrator" ) {
                    binding.layoutUsers.isVisible = true
                    binding.layoutProduct.isVisible = true
                }
            }
        }
    }

    private fun onUpdateView(entities: List<ProductsModel>) {
        binding.apply {
            val adapter = FavoriteAdapter.FavoriteAdapter(entities, onFavoriteClick =  {
                viewModel.changeISInFavorite(it)
            }, Perexod = {
                intent =
                    Intent(this@FavoriteActivity, DetailProductActivity::class.java).apply {
                        putExtra("product_id", it.id)
                    }
                startActivity(intent)
            })
            rcViewFavorite.adapter = adapter
            rcViewFavorite.layoutManager = GridLayoutManager(this@FavoriteActivity,2)

        }


    }



}