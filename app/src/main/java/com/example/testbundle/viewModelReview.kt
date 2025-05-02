package com.example.testbundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.Repository.CategoryRepository
import com.example.testbundle.Repository.FavoriteRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.Repository.ReviewsRepository
import com.example.testbundle.db.Category
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import com.example.testbundle.db.Reviews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewViewModel(productId:Int) : ViewModel() {






    fun deleteReview(id: Int) {
        viewModelScope.launch {
            repos.deleteReviews(id)
        }
    }
    fun updateReview(item: Reviews) {
        viewModelScope.launch {
            repos.updateReviews(item)
        }
    }
    fun insertReviews(item: Reviews){
        viewModelScope.launch {
            repos.insertReviews(item)
        }
    }

    private val repos = ReviewsRepository.getInstance()

    private val _stateReview: MutableStateFlow<List<Reviews>> = MutableStateFlow(emptyList())
    val stateReviews: StateFlow<List<Reviews>>
        get() = _stateReview.asStateFlow()

    init {
        viewModelScope.launch {
            repos.getReviewsById(productId).collect { list ->
                _stateReview.update {
                    list
                }
            }
        }
    }

}