import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Repository.ReviewsRepository
import com.example.testbundle.db.Reviews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReviewViewModel : ViewModel() {
    private val repos = ReviewsRepository.getInstance()

    private val _stateReview: MutableStateFlow<List<Reviews>> = MutableStateFlow(emptyList())
    val stateReviews: StateFlow<List<Reviews>> = _stateReview.asStateFlow()

    private var currentProductId: Int? = null
    private val productApi = RetrofitClient.apiService
    fun loadReviews(productId: Int) {
        currentProductId = productId
        viewModelScope.launch {
            val reviews = productApi.getReviewByProductId(productId)
                _stateReview.update { reviews }

        }
    }

    fun deleteReview(id: Int) {
        viewModelScope.launch {
            productApi.deleteReviews(id)
            currentProductId?.let { loadReviews(it) } // Перезагружаем отзывы после удаления
        }
    }

    fun updateReview(id:Int,item: Reviews) {
        viewModelScope.launch {
            productApi.updateReviews(id,item)
            currentProductId?.let { loadReviews(it) }
        }
    }

    fun insertReviews(item: Reviews) {
        viewModelScope.launch {
            productApi.insertReviews(item)
            currentProductId?.let { loadReviews(it) }
        }
    }
}