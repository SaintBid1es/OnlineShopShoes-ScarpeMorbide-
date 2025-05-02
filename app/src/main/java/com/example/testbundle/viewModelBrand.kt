package com.example.testbundle





import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.Repository.FavoriteRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.Products
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrandViewModel(
) : ViewModel() {






    fun deleteBrand(id: Int) {
        viewModelScope.launch {
            repos.deleteBrand(id)
        }
    }
    fun updateBrand(item: Brand) {
        viewModelScope.launch {
            repos.updateBrand(item)
        }
    }
    fun insertBrand(item: Brand){
        viewModelScope.launch {
            repos.insertBrand(item)
        }
    }

    private val repos = BrandRepository.getInstance()

    private val _stateBrand: MutableStateFlow<List<Brand>> = MutableStateFlow(emptyList())

    val stateBrand: StateFlow<List<Brand>>
        get() = _stateBrand.asStateFlow()

    init {
        viewModelScope.launch {
            repos.getBrand().collect { list ->
                _stateBrand.update {
                    list
                }
            }
        }
    }

}