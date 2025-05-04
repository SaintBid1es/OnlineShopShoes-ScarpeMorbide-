package com.example.testbundle.Activity.Admin

import ProductCardAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.User.BasketActivity
import com.example.testbundle.Activity.User.FavoriteActivity
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.Activity.User.OrderHistoryActivity
import com.example.testbundle.Activity.User.ProfileActivity
import com.example.testbundle.Adapter.ProductCardUserAdapter
import com.example.testbundle.ProductViewModel
import com.example.testbundle.databinding.ActivityListProductAdminBinding
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.launch

class ListProductAdminActivity : BaseActivity() {
    lateinit var binding: ActivityListProductAdminBinding

    val viewModel: ProductViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListProductAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rcView.layoutManager = LinearLayoutManager(this)


        /**
         * Вывод списка пользователей для администратора
         */

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateProduct?.collect {
                    onUpdateView(it)
                }
            }
        }

        /**
         * Навигационная панель
         */
        binding.imgFavorite.setOnClickListener {
            startActivity(Intent(this@ListProductAdminActivity, FavoriteActivity::class.java))
        }
        binding.imgBasket.setOnClickListener {
            startActivity(Intent(this@ListProductAdminActivity, BasketActivity::class.java))
        }
        binding.imgProfile.setOnClickListener {
            startActivity(Intent(this@ListProductAdminActivity, ProfileActivity::class.java))
        }
        binding.imgMain.setOnClickListener {
            startActivity(Intent(this@ListProductAdminActivity, ListProductActivity::class.java))
        }
        binding.imgClients.setOnClickListener {
            startActivity(Intent(this@ListProductAdminActivity, ListEmployeeActivity::class.java))
        }

        binding.imgCreateProduct.setOnClickListener {
            startActivity(Intent(this@ListProductAdminActivity, CreateProductActivity::class.java))

        }
        binding.imgOrderHistory.setOnClickListener {
            startActivity(Intent(this@ListProductAdminActivity, OrderHistoryActivity::class.java))
        }

    }

    /**
     * Вывод списка товаров для администратора
     */
    private fun onUpdateView(entities: List<ProductsModel>) {
        binding.apply {
            val db = MainDb.getDb(this@ListProductAdminActivity)
            rcView.adapter = ProductCardAdapter(entities, onEdit = {
                intent = Intent(this@ListProductAdminActivity, UpdateProductActivity::class.java).apply {
                    putExtra("product_id",it.id)
                }
                startActivity(intent)
            },
                onDelete = { id ->
                    viewModel.deleteProduct(id)
                })

        }
    }
}