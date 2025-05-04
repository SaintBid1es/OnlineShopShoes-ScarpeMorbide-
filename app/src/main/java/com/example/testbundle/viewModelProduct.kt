package com.example.testbundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.Repository.CategoryRepository
import com.example.testbundle.Repository.FavoriteRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {


    private val productRepo = ProductRepository.getInstance()
    private val favoriteRepo = FavoriteRepository.getInstance()
    private val brandRepo = BrandRepository.getInstance()
    private val categoryRepo = CategoryRepository.getInstance()
    private val authRepo = DataStoreRepo.getInstance()


    private val _stateProduct = MutableStateFlow<List<ProductsModel>>(emptyList())
    val stateProduct: StateFlow<List<ProductsModel>> get() = _stateProduct.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Int?>(null)
    private val _selectedBrand = MutableStateFlow<Int?>(null)

    init {
        loadProducts()
    }


    private fun loadProducts() {
        viewModelScope.launch {
            authRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userID ->
                val products = productRepo.getProducts()
                val filteredProducts = applyFilters(products)
                _stateProduct.update {
                    filteredProducts.map { product ->
                        val isInFavorite = favoriteRepo.getIsInFavorite(userID, product.id ?: 0) > 0
                        ProductsModel(
                            id = product.id,
                            name = product.name,
                            description = product.description,
                            cost = product.cost,
                            imageId = product.imageId,
                            isFavorite = isInFavorite,
                            product.imageUri
                        )
                    }
                }
            }
        }
    }


    private fun applyFilters(products: List<Products>): List<Products> {
        return products.filter { product ->
            val matchesCategory =
                _selectedCategory.value == null || product.category == _selectedCategory.value
            val matchesBrand =
                _selectedBrand.value == null || product.brandId == _selectedBrand.value
            matchesCategory && matchesBrand
        }
    }


    fun setCategoryFilter(categoryName: String?) {
        viewModelScope.launch {
            _selectedCategory.update {
                categoryName?.let {
                    categoryRepo.getCategoryByName(categoryName)?.id
                }
            }
            loadProducts()
        }
    }




    fun setBrandFilter(brandName: String?) {
        viewModelScope.launch {
            _selectedBrand.update {
                brandName?.let {
                    brandRepo.getBrandByName(brandName)?.id
                }
            }
            loadProducts()
        }
    }


    fun insertProduct(product: Products) {
        viewModelScope.launch {
            productRepo.insertProduct(product)
        }
    }


    fun updateProduct(product: Products) {
        viewModelScope.launch {
            productRepo.updateProduct(product)
        }
    }


    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            productRepo.deleteProduct(productId)
        }.invokeOnCompletion {
            loadProducts()
        }
    }


    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            authRepo.dataStoreFlow.first()[DataStoreRepo.USER_ID_KEY]?.let { userID ->
                val isInFavorite = favoriteRepo.getIsInFavorite(userID, productId) > 0
                if (isInFavorite) {
                    favoriteRepo.deleteClientItemByProduct(userID, productId)
                } else {
                    favoriteRepo.insertFavorite(
                        Favorite(
                            client_id = userID,
                            product_id = productId
                        )
                    )
                }
                _stateProduct.update { currentList ->
                    currentList.map { product ->
                        if (product.id == productId) {
                            product.copy(isFavorite = !isInFavorite)
                        } else {
                            product
                        }
                    }
                }
            }
        }
    }
}