package com.example.testbundle

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.OrderRepository
import com.example.testbundle.Repository.OrderItemsRepository
import com.example.testbundle.db.Order
import com.example.testbundle.db.OrderItem
import com.example.testbundle.db.OrderModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class OrderViewModel(
) : ViewModel() {


    private var orderRepository = OrderRepository.getInstance()
    private val dataStoreRepo = DataStoreRepo.getInstance()

    private val _orderItems: MutableStateFlow<List<OrderModel>> =
        MutableStateFlow(emptyList())
    val orderItems: StateFlow<List<OrderModel>> get() = _orderItems.asStateFlow()
    private val repos = OrderRepository.getInstance()
    private val repos1 = OrderItemsRepository.getInstance()

    /**
     *Инициализация списка
     */
    init {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.collect {
                it[DataStoreRepo.USER_ID_KEY]?.let { userId ->
                    repos.getOrdersByUser(userId).collect { orders ->
                        _orderItems.update {
                            orders.map { order ->
                                OrderModel(
                                    id = order.id,
                                    orderDate = order.orderDate,
                                    totalPrice = order.totalPrice,
                                    products = repos1.getProductsByOrderId(order.id)
                                        .joinToString(separator = "\n") { item ->
                                            "${item.productName} x${item.quantity} - %s${item.price * item.quantity} (${item.size})"
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Функция добавления заказа
     * @param order[Order],onOrderInserted[UUID]
     */
    fun insertOrder(order: Order, onOrderInserted: (UUID?) -> Unit) {
    viewModelScope.launch {
        val orderId = orderRepository.insertItem(order)
        onOrderInserted(orderId)
    }
}

    /**
     * Функция удаления заказа
     */
    fun deleteOrder() {
    viewModelScope.launch {
        repos.deleteItem()
        repos1.deleteItem()
    }
}




}