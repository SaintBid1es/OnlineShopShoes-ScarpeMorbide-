package com.example.testbundle.Activity.User

import ReviewViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testbundle.Activity.User.ProfileActivity.Companion.idAccount
import com.example.testbundle.Adapter.ReviewAdapter
import com.example.testbundle.databinding.ActivityReviewsBinding
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Reviews
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReviewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewsBinding
    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
            startActivity(Intent(this@ReviewsActivity,DetailProductActivity::class.java))
        }
    }

    private fun onUpdateView(entities: List<Reviews>) {
        val db = MainDb.getDb(this)

        // Запускаем корутину для работы с Flow
        lifecycleScope.launch {
            // Получаем список клиентов из Flow
            val clients = db.getDao().getAllItems().first() // first() получает первый эмит из Flow

            // Создаем маппинг ID -> имя клиента
            val clientNames = clients.associate { it.id to it.Name }

            // Инициализируем адаптер с передачей clientNames
            binding.rcViewReview.adapter = ReviewAdapter(
                clientID = idAccount,
                entities = entities,
                clientNames = clientNames, // Передаем маппинг имен
                onDelete = { id -> viewModel.deleteReview(id) },
                onEdit = { review ->
                    startActivity(Intent(this@ReviewsActivity, UpdateReviewActivity::class.java).apply {
                        putExtra("review_id", review.id)
                    })
                }
            )
        }
    }
}