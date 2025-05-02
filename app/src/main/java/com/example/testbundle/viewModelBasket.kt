package com.example.testbundle


import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.BasketRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.db.Basket
import com.example.testbundle.db.BasketModel
import com.example.testbundle.db.Dao
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BasketViewModel(
) : ViewModel() {


    private val repo = BasketRepository.getInstance()
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
            repo.deleteItem()
        }
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
                    repo.deleteClientItemByProduct(it, productID,sizeID)
                }
            }
        }
    }


    fun updateItem(item: Basket) {
        viewModelScope.launch {
            repo.updateItem(item)
        }
    }

    /**
     *  Функция добавления кол-ва товара в корзину
     */
    fun increaseQuantity(productId: Int) {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userId ->
                val basketItem = repo.getBasketItemByProduct(userId, productId)
                if (basketItem != null) {
                    val updatedItem = basketItem.copy(count = basketItem.count + 1)
                    repo.updateItem(updatedItem)
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
                val basketItem = repo.getBasketItemByProduct(userId, productId)
                if (basketItem != null) {
                    val updatedItem = basketItem.copy(count = basketItem.count - 1)
                    repo.updateItem(updatedItem)
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

                val existingCount = repo.getBasketItemByProductAndSize(
                    item.size,
                    item.product_id,
                    item.client_id
                ) ?: 0

                if (existingCount > 0) {

                    Message.postValue(errorMessage)
                } else {

                    repo.insertItem(item)
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
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userId ->
                repo.getItemsByUser(userId).collect { list ->
                    _state.update {
                        list.mapNotNull { record ->
                            var product = productsRepo.getProductById(record.product_id)
                            BasketModel(product!!.id, product.name, product.description, product.cost, product.imageId, record.count,record.size)
                        }
                    }
                }
            }
        }
    }

    init {
        loadBasketItems() 
    }


}