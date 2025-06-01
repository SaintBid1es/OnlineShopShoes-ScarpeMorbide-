package com.example.testbundle


import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.BasketRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.db.Basket
import com.example.testbundle.db.BasketModel
import com.example.testbundle.db.Dao
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import com.example.testbundle.db.Reviews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BasketViewModel(
) : ViewModel() {


    private val repo = BasketRepository.getInstance()
    private val productApi = RetrofitClient.apiService
    private val productsRepo = ProductRepository.getInstance()
    private val dataStoreRepo = DataStoreRepo.getInstance()

    private var Message = MutableLiveData<String?>()
    val Messages: LiveData<String?> get() = Message

    private val _state: MutableStateFlow<List<BasketModel>> = MutableStateFlow(emptyList())
    val stateBasket: StateFlow<List<BasketModel>>
        get() = _state.asStateFlow()
    private val _count = MutableLiveData<Int>(1)
    val count: LiveData<Int> get() = _count


    /**
     * Функция чистки корзины
     */
    fun deleteItem() {
        viewModelScope.launch {
                productApi.deleteTableBaskets()
        }
        loadBasketItems()
    }

    suspend fun isProductInBasket(productId: Int, userId: Int): Boolean {
        val count = repo.isProductInBasket(productId, userId) ?: 0
        return count > 0
    }

    /**
     * Функция удаления продукта
     */
    fun deleteItemByProductId(productID: Int,sizeID:Int) {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.collect {
                it[DataStoreRepo.USER_ID_KEY]?.let {
                    productApi.deleteClientItemByProductBasket(it, productID,sizeID)
                }
            }
        }
    }

    suspend fun calculateTotalRating(id_product:Int):Double {
        var selectRating:Double=0.0
        val countrating = productApi.countRating(id_product)
        if (countrating!=0){
             selectRating =productApi.selectRating(id_product)
        }

        return selectRating/countrating
    }
    suspend fun countProductById(id_product:Int):Int{
        val countrating = productApi.countRating(id_product)
        return countrating
    }

    fun updateItem(id:Int,item: Basket) {
        viewModelScope.launch {
            productApi.updateBaskets(id,item)
        }
    }

    /**
     *  Функция добавления кол-ва товара в корзину
     */
    fun increaseQuantity(productId: Int) {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userId ->
                val basketItem = productApi.getBasketItemByProduct(userId, productId)
                val product = productApi.getProductsByID(productId)
                    if (product.amount>basketItem!!.countbasket) {
                        val updatedItem = basketItem.copy(countbasket = basketItem.countbasket + 1)
                        productApi.updateBaskets(updatedItem.id!!, updatedItem)
                        loadBasketItems()
                    }
            }
        }
    }
    /**
     *  Функция уменьшения кол-ва товара в корзины
     */
    fun decreaseQuantity(productId: Int) {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userId ->
                val basketItem = productApi.getBasketItemByProduct(userId, productId)
                if (basketItem != null) {
                    val updatedItem = basketItem.copy(countbasket = basketItem.countbasket - 1)
                    productApi.updateBaskets(updatedItem.id!!,updatedItem)
                    loadBasketItems()
                }
            }
        }
    }

    /**
     * Функция подсчета итоговой суммы
     */
    fun calculateTotalPrice(items: List<BasketModel>): Double {
        return items.sumOf { it.cost * it.count }
    }

    /**
     * Функция добавления в корзину
     */
    fun insertItem(item: Basket, successMessage: String, errorMessage: String) {
        viewModelScope.launch {
            try {
                val existingCount = productApi.getBasketItemByProductAndSize(
                    item.size,
                    item.productId,
                    item.clientId
                ) ?: 0

                if (existingCount > 0) {
                    Message.postValue(errorMessage)
                } else {

                    productApi.insertBasket(item)
                    Message.postValue(successMessage)
                }
            } catch (ex: Exception) {
                Log.e("sql", ex.message ?: "Sql error")
                Message.postValue("Ошибка при добавлении в корзину")
            }
        }
    }




    private fun loadBasketItems() {
        viewModelScope.launch {
            try {
                // Получаем userId из DataStore
                val userId = dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY] ?: return@launch

                // Получаем список товаров в корзине
                val basketItems = productApi.getItemsByUser(userId)

                // Преобразуем в список BasketModel
                val basketModels = basketItems.mapNotNull { basketItem ->
                    val product = try {
                        productApi.getProductsByID(basketItem.productId)
                    } catch (e: Exception) {
                        null // Пропускаем товары, которые не удалось загрузить
                    } ?: return@mapNotNull null

                    BasketModel(
                        id = product.id ?: -1,
                        name = product.name.orEmpty(),
                        description = product.description.orEmpty(),
                        cost = product.cost ?: 0.0,
                        imageId = product.imageid,
                        imageUri = product.imageuri.orEmpty(),
                        count = basketItem.countbasket,
                        size = basketItem.size,
                        amount = product.amount ?: 0,
                        brand = product.brandid ?: -1,
                        category = product.categoryid ?: -1
                    )
                }

                // Обновляем состояние
                _state.update { basketModels }

            } catch (e: Exception) {
                // Обработка ошибок (можно добавить errorState)
                _state.update { emptyList() }
            }
        }
    }

    init {
        loadBasketItems()
    }


}