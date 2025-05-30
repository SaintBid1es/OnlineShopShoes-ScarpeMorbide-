package com.example.testbundle






import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.Repository.FavoriteRepository
import com.example.testbundle.Repository.OrderItemsRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.OrderItem
import com.example.testbundle.db.Products
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OrderItemViewModel(
) : ViewModel() {




    private val productApi = RetrofitClient.apiService

    fun deleteOrderItem(id: Int) {
        viewModelScope.launch {
            productApi.deleteOrderItems(id)
            loadOrderItems()
        }
    }
    fun updateOrderItem(id:Int,item: OrderItem) {
        viewModelScope.launch {
            productApi.updateOrderItems(id,item)
            loadOrderItems()
        }
    }
    fun insertOrderItem(item: OrderItem){
        viewModelScope.launch {
            productApi.insertOrderItems(item)
            loadOrderItems()
        }
    }
    fun loadOrderItems(){
        viewModelScope.launch {
            repos.getItems().collect { list ->
                _stateOrderItem.update {
                    list
                }
            }
        }
    }

    private val repos = OrderItemsRepository.getInstance()

    private val _stateOrderItem: MutableStateFlow<List<OrderItem>> = MutableStateFlow(emptyList())

    val stateOrderItem: StateFlow<List<OrderItem>>
        get() = _stateOrderItem.asStateFlow()

    init {
        loadOrderItems()
    }

}