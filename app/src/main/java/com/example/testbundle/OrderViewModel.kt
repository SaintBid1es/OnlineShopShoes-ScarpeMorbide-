package com.example.testbundle


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    suspend fun getProductSalesStatistics(db: MainDb): List<SalesData> {
        val orderItems = db.getDao().getAllOrderItem().first()
        val productSales = mutableMapOf<String, Int>()

        orderItems.forEach { orderItem ->
            val productName = orderItem.productName
            val quantity = orderItem.quantity
            productSales[productName] = productSales.getOrDefault(productName, 0) + quantity
        }

        return productSales.entries
            .sortedByDescending { it.value }
            .map { SalesData(it.key, it.value, "product") }
    }
    // OrderViewModel.kt

    // Для статистики по брендам
    suspend fun getBrandSalesStatistics(db: MainDb): List<Pair<String, Int>> {
        val orderItems = db.getDao().getAllOrderItem().first()
        val brandSales = mutableMapOf<String, Int>()  // Название бренда -> количество

        orderItems.forEach { orderItem ->
            val product = db.getDao().getProductById(orderItem.productId)
            product?.brandId?.let { brandId ->
                val brand = db.getDao().getBrandById(brandId)  // Получаем бренд по ID
                brand?.name?.let { brandName ->
                    brandSales[brandName] = brandSales.getOrDefault(brandName, 0) + orderItem.quantity
                }
            }
        }

        return brandSales.entries.map { Pair(it.key, it.value) }
    }

    // Для статистики по категориям
    suspend fun getCategorySalesStatistics(db: MainDb): List<Pair<String, Int>> {
        val orderItems = db.getDao().getAllOrderItem().first()
        val categorySales = mutableMapOf<String, Int>()  // Название категории -> количество

        orderItems.forEach { orderItem ->
            val product = db.getDao().getProductById(orderItem.productId)
            product?.category?.let { categoryId ->
                val category = db.getDao().getCategoryById(categoryId)  // Получаем категорию по ID
                category?.name?.let { categoryName ->
                    categorySales[categoryName] = categorySales.getOrDefault(categoryName, 0) + orderItem.quantity
                }
            }
        }

        return categorySales.entries.map { Pair(it.key, it.value) }
    }
    // OrderViewModel.kt

    suspend fun getCombinedBrandCategoryStatsOptimized(db: MainDb): List<Pair<String, Int>> {
        // Получаем все необходимые данные за один раз
        val orderItems = db.getDao().getAllOrderItem().first()
        val productIds = orderItems.map { it.productId }.distinct()
        val products = db.getDao().getProductsByIds(productIds)

        val brandIds = products.mapNotNull { it.brandId }.distinct()
        val brands = db.getDao().getBrandsByIds(brandIds).associateBy { it.id }

        val categoryIds = products.mapNotNull { it.category }.distinct()
        val categories = db.getDao().getCategoriesByIds(categoryIds).associateBy { it.id }

        val stats = mutableMapOf<String, Int>()

        for (item in orderItems) {
            val product = products.find { it.id == item.productId } ?: continue
            val quantity = item.quantity

            // Бренд
            product.brandId?.let { brandId ->
                brands[brandId]?.name?.let { brandName ->
                    val key = "Бренд: $brandName"
                    stats[key] = stats.getOrDefault(key, 0) + quantity
                }
            }

            // Категория
            product.category?.let { categoryId ->
                categories[categoryId]?.name?.let { categoryName ->
                    val key = "Категория: $categoryName"
                    stats[key] = stats.getOrDefault(key, 0) + quantity
                }
            }
        }

        return stats.toList().sortedByDescending { it.second }
    }
    // OrderViewModel.kt
    suspend fun getRevenueStatistics(db: MainDb): Triple<Double, Double, Double> {
        val daily = db.getDao().getDailyRevenue() ?: 0.0
        val monthly = db.getDao().getMonthlyRevenue() ?: 0.0
        val yearly = db.getDao().getYearlyRevenue() ?: 0.0
        return Triple(daily, monthly, yearly)
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