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
                   productApi.deleteFavoriteByIdClientAndProduct(userID, productID)
                    loadFavorite()
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
       loadFavorite()
    }
    fun loadFavorite(){
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userID ->
                val favorite =  productApi.getFavoritesByID(userID)
                flow{
                    emit(favorite.mapNotNull { favorite->
                        productApi.getProductsByID(favorite.productId)?.let { product ->
                            val isInFavorite = productApi.getIsInFavorite(userID, product.id ?: -1)
                            ProductsModel(
                                product.id ?: -1,
                                product.name,
                                product.description,
                                product.cost,
                                product.imageid,
                                isInFavorite > 0,
                                product.imageuri

                            )
                        }
                    })
                }
            }?.collect { list ->
                _state.update {
                    list
                }
            }
        }
    }

}