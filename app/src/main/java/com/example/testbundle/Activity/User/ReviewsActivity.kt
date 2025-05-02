import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.testbundle.Activity.User.UpdateReviewActivity
import com.example.testbundle.Adapter.ReviewAdapter
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityReviewsBinding
import com.example.testbundle.db.Reviews
import kotlinx.coroutines.launch

class ReviewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewsBinding
    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("product_id", -1)
        if (productId == -1) {
            finish()
            return
        }


        viewModel.loadReviews(productId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateReviews.collect { reviews ->
                    onUpdateView(reviews)
                }
            }
        }
    }

    private fun onUpdateView(entities: List<Reviews>) {
        binding.apply {
            rcViewReview.adapter = ReviewAdapter(
                entities,
                onEdit = {
                    val intent = Intent(this@ReviewsActivity, UpdateReviewActivity::class.java).apply {
                        putExtra("review_id", it.id)
                    }
                    startActivity(intent)
                },
                onDelete = { id ->
                    viewModel.deleteReview(id)
                }
            )
        }
    }
}