package com.example.testbundle


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.FavoriteRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavoriteViewModel(
) : ViewModel() {

    private val productApi = RetrofitClient.apiService
    fun changeISInFavorite(productID: Int) {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userID ->
                val isInFavorite = if (productApi.getIsInFavorite(userID, productID) > 0) {
                   repos.deleteClientItemByProduct(userID, productID)
                    false
                } else {
                    productApi.insertFavorites(Favorite(clientId = userID, productId = productID))
                    true
                }

                _state.update {
                    it.apply {
                        it.toMutableList()[it.indexOfFirst { r -> r.id == productID }] =
                            it.first { r -> r.id == productID }.copy(isFavorite = isInFavorite)
                    }
                }
            }
        }
    }


    private val repos = FavoriteRepository.getInstance()
    private val productsRepo = ProductRepository.getInstance()
    private val dataStoreRepo = DataStoreRepo.getInstance()
    private val _state: MutableStateFlow<List<ProductsModel>> = MutableStateFlow(emptyList())
    val stateFavorite: StateFlow<List<ProductsModel>>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userId ->
                // Получаем данные один раз
                val baskets = productApi.getItemsByUser(userId)

                // Преобразуем в Flow
                flow {
                    emit(baskets.mapNotNull { basket ->
                        productApi.getProductsByID(basket.productId)?.let { product ->
                            val isFavorite = productApi.getIsInFavorite(userId, product.id ?: -1)
                            ProductsModel(
                                product.id ?: -1,
                                product.name,
                                product.description,
                                product.cost,
                                product.imageid,
                                isFavorite > 0,
                                product.imageuri

                            )
                        }
                    })
                }.collect { products ->
                    _state.update { products }
                }
            }
        }
    }

}