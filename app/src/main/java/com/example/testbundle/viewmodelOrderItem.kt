package com.example.testbundle






import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class OrderItemViewModel(
) : ViewModel() {






    fun deleteOrderItem(id: Int) {
        viewModelScope.launch {
            repos.deleteOrderItemById(id)
        }
    }
    fun updateOrderItem(item: OrderItem) {
        viewModelScope.launch {
            repos.updateItem(item)
        }
    }
    fun insertOrderItem(item: OrderItem){
        viewModelScope.launch {
            repos.insertItem(item)
        }
    }

    private val repos = OrderItemsRepository.getInstance()

    private val _stateOrderItem: MutableStateFlow<List<OrderItem>> = MutableStateFlow(emptyList())

    val stateOrderItem: StateFlow<List<OrderItem>>
        get() = _stateOrderItem.asStateFlow()

    init {
        viewModelScope.launch {
            repos.getItems().collect { list ->
                _stateOrderItem.update {
                    list
                }
            }
        }
    }

}