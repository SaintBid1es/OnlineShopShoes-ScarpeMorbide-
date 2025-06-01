package com.example.testbundle.Activity.User

import ReviewViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.User.ProfileActivity.Companion.idAccount
import com.example.testbundle.Adapter.ReviewAdapter
import com.example.testbundle.MainViewModel
import com.example.testbundle.Repository.AuthRepository
import com.example.testbundle.databinding.ActivityReviewsBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Reviews
import com.example.testbundle.withAuthToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReviewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewsBinding
    private val viewModel: ReviewViewModel by viewModels()
    private lateinit var authRepository: AuthRepository
    private val productApi = RetrofitClient.apiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(applicationContext)
        binding.rcViewReview.apply {
            layoutManager = LinearLayoutManager(this@ReviewsActivity)
            setHasFixedSize(true)
        }

        val productId = intent.getIntExtra("product_id", -1)
        if (productId == -1) {
            finish()
            return
        }


        viewModel.loadReviews(productId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateReviews.collect { reviews ->
                    if (reviews.isNotEmpty()) {
                        onUpdateView(reviews)
                    } else {

                    }
                }
            }
        }
        binding.btnArrowBack.setOnClickListener{
            val productId = intent.getIntExtra("product_id", -1)
            val i = Intent(this@ReviewsActivity, DetailProductActivity::class.java)
            i.putExtra("product_id",productId)
            startActivity(i)
        }



    }

    private fun onUpdateView(entities: List<Reviews>) {


        // Запускаем корутину для работы с Flow
        lifecycleScope.launch {
            withAuthToken { token ->
                val clients = productApi.getUsers(token) // first() получает первый эмит из Flow


                // Создаем маппинг ID -> имя клиента
                val clientNames = clients.associate { it.id to it.name }
                var userAvatar = clients.associate { it.id to it.avatar.toString().toUri() }

                // Инициализируем адаптер с передачей clientNames
                binding.rcViewReview.adapter = ReviewAdapter(
                    clientID = idAccount,
                    entities = entities,
                    clientAvatar = userAvatar,
                    clientNames = clientNames, // Передаем маппинг имен
                    onDelete = { id -> viewModel.deleteReview(id) },
                    onEdit = { review ->
                        startActivity(
                            Intent(
                                this@ReviewsActivity,
                                UpdateReviewActivity::class.java
                            ).apply {
                                putExtra("review_id", review.id)
                            })
                    }
                )
            }
        }
    }
}