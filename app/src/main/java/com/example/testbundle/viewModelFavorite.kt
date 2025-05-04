package com.example.testbundle


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.FavoriteRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteViewModel(
) : ViewModel() {




    fun changeISInFavorite(productID: Int) {
        viewModelScope.launch {
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userID ->
                val isInFavorite = if (repos.getIsInFavorite(userID, productID) > 0) {
                    repos.deleteClientItemByProduct(userID, productID)
                    false
                } else {
                    repos.insertFavorite(Favorite(client_id = userID, product_id = productID))
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
            dataStoreRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userID ->
                repos.getItemsByUser(userID).collect { list ->
                    _state.update {
                        list.mapNotNull {
                            productsRepo.getProductById(it.product_id)?.let { product ->
                                val isInFavorite = repos.getIsInFavorite(userID, product.id ?: -1)
                                ProductsModel(
                                    product.id ?: -1,
                                    product.name,
                                    product.description,
                                    product.cost,
                                    product.imageId,
                                    isInFavorite > 0,
                                    product.imageUri

                                )
                            }
                        }
                    }

                }
            }
        }
    }

}