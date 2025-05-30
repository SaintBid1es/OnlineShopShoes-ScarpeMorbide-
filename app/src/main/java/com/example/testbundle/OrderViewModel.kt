package com.example.testbundle


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Activity.User.BasketActivity
import com.example.testbundle.Repository.OrderRepository
import com.example.testbundle.Repository.OrderItemsRepository
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Order
import com.example.testbundle.db.OrderItem
import com.example.testbundle.db.OrderModel
import com.example.testbundle.db.SalesData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    private val productApi = RetrofitClient.apiService
    /**
     *Инициализация списка
     */
    init {
       loadOrder()
    }
    fun loadOrder(){
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.collect { preferences ->
                try {
                    val userId = preferences[DataStoreRepo.USER_ID_KEY] ?: return@collect

                    // Получаем заказы клиента
                    val orders = productApi.getOrderByClient(userId)

                    // Преобразуем каждый заказ в OrderModel
                    val orderModels = orders.map { order ->
                        val orderItems = try {
                            productApi.getProductOrderItemById(order.id)
                        } catch (e: Exception) {
                            emptyList() // В случае ошибки используем пустой список
                        }

                        OrderModel(
                            id = order.id,
                            orderDate = order.orderdate.orEmpty(),
                            totalPrice = order.totalprice ?: 0.0,
                            products = orderItems.joinToString("\n") { item ->
                                "${item.productname.orEmpty()} x${item.quantity} - $${(item.price ?: 0.0) * item.quantity} (${item.size})"
                            }
                        )
                    }

                    _orderItems.update { orderModels }

                } catch (e: Exception) {
                    // Обработка ошибок (можно добавить errorState)
                    _orderItems.update { emptyList() }
                }
            }
        }
    }
    suspend fun getProductSalesStatistics(): List<SalesData> {
        val orderItems = productApi.getOrderItems()
        val productSales = mutableMapOf<String, Int>()

        orderItems.forEach { orderItem ->
            val productName = orderItem.productname
            val quantity = orderItem.quantity
            productSales[productName] = productSales.getOrDefault(productName, 0) + quantity
        }

        return productSales.entries
            .sortedByDescending { it.value }
            .map { SalesData(it.key, it.value, "product") }
    }
    // OrderViewModel.kt

    // Для статистики по брендам
    suspend fun getBrandSalesStatistics(): List<Pair<String, Int>> {
        val orderItems = productApi.getOrderItems()
        val brandSales = mutableMapOf<String, Int>()  // Название бренда -> количество

        orderItems.forEach { orderItem ->
            val product = productApi.getProductsByID(orderItem.productid)
            product?.brandid?.let { brandId ->
                val brand = productApi.getBrandsByID(brandId)  // Получаем бренд по ID
                brand?.namebrand?.let { brandName ->
                    brandSales[brandName] = brandSales.getOrDefault(brandName, 0) + orderItem.quantity
                }
            }
        }

        return brandSales.entries.map { Pair(it.key, it.value) }
    }


    // OrderViewModel.kt

    suspend fun getCombinedBrandCategoryStatsOptimized(): List<Pair<String, Int>> {
        // Получаем все необходимые данные за один раз
        val orderItems = productApi.getOrderItems()
        val productIds = orderItems.map { it.productid }.distinct()
         val products = productApi.GetProductsByIds(productIds)

        val brandIds = products.mapNotNull { it.brandid }.distinct()
        val brands = productApi.GetBrandsByIds(brandIds).associateBy { it.id }

        val categoryIds = products.mapNotNull { it.categoryid }.distinct()
        val categories = productApi.getCategoryByIds(categoryIds).associateBy { it.id }

        val stats = mutableMapOf<String, Int>()

        for (item in orderItems) {
            val product = products.find { it.id == item.productid } ?: continue
            val quantity = item.quantity

            // Бренд
            product.brandid?.let { brandId ->
                brands[brandId]?.namebrand?.let { brandName ->
                    val key = "Бренд: $brandName"
                    stats[key] = stats.getOrDefault(key, 0) + quantity
                }
            }

            // Категория
            product.categoryid?.let { categoryId ->
                categories[categoryId]?.namecategory?.let { categoryName ->
                    val key = "Категория: $categoryName"
                    stats[key] = stats.getOrDefault(key, 0) + quantity
                }
            }
        }

        return stats.toList().sortedByDescending { it.second }
    }
    // OrderViewModel.kt
    suspend fun getRevenueStatistics(): Triple<Double, Double, Double> {
        val daily = productApi.getDailyRevenue() ?: 0.0
        val monthly = productApi.getMonthlyRevenue() ?: 0.0
        val yearly = productApi.getYearlyRevenue() ?: 0.0
        return Triple(daily, monthly, yearly)
    }


    /**
     * Функция добавления заказа
     * @param order[Order],onOrderInserted[UUID]
     */
    fun insertOrder(order: Order, onOrderInserted: (UUID?) -> Unit) {
    viewModelScope.launch {
        val orderId = orderRepository.insertItem(order) //NORM Product_api
        onOrderInserted(orderId)
    }
}

    /**
     * Функция удаления заказа
     */
    fun deleteOrder() {
    viewModelScope.launch {
        productApi.deleteTableOrders()
        productApi.deleteTableOrderItems()
    }
        loadOrder()

}





}