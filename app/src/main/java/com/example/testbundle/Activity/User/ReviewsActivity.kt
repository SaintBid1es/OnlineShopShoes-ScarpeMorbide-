package com.example.testbundle.Activity.User

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testbundle.Activity.Admin.EditAccountCardActivity
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.Adapter.AccountCardAdapter
import com.example.testbundle.Adapter.ProductCardUserAdapter
import com.example.testbundle.Adapter.ReviewAdapter
import com.example.testbundle.FavoritePreferences
import com.example.testbundle.MainViewModel
import com.example.testbundle.R
import com.example.testbundle.ReviewViewModel
import com.example.testbundle.databinding.ActivityReviewsBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.Reviews
import kotlinx.coroutines.launch

class ReviewsActivity : AppCompatActivity() {
   private lateinit var binding : ActivityReviewsBinding
    val viewModel : ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)






        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel?.stateReviews?.collect{
                    onUpdateView(it)
                }
            }
        }


    }
    private fun onUpdateView(entities: List<Reviews>) {
        binding.apply {
          //  val filteredEntities = entities.filter { it.id != currentUserId }
            rcViewReview.adapter = ReviewAdapter(
                entities,
                onEdit = {
                    intent = Intent(this@ReviewsActivity, UpdateReviewActivity::class.java).apply {
                        putExtra("review_id", it.id)
                    }
                    startActivity(intent)
                },
                onDelete = { id ->
                    viewModel?.deleteReview(id)
                }
            )
        }
    }


}
