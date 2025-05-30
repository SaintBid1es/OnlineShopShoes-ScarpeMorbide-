package com.example.testbundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.db.Brand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BrandViewModel : ViewModel() {

    private val repos = BrandRepository.getInstance()

    private val _stateBrand: MutableStateFlow<List<Brand>> = MutableStateFlow(emptyList())
    val stateBrand: StateFlow<List<Brand>> = _stateBrand.asStateFlow()

    private val productApi = RetrofitClient.apiService

    init {
        loadBrands()
    }

    private fun loadBrands() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val brands = productApi.getBrands()
                _stateBrand.update { brands }
            } catch (e: Exception) {
                // Обработка ошибки
                e.printStackTrace()
            }
        }
    }
    fun checkAndInsertBrand(name: String, onExists: () -> Unit, onSuccess: (Brand) -> Unit) {
        viewModelScope.launch {
            val brands = productApi.getBrands()
            val brandExists = brands.any { it.namebrand == name }

            if (brandExists) {
                onExists()
            } else {
                val newBrand = Brand(null, name)
                insertBrand(newBrand)
                onSuccess(newBrand)
            }
        }
    }

    fun deleteBrand(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            productApi.deleteBrands(id)
            loadBrands() // Обновляем список после удаления
        }
    }

    fun updateBrand(id: Int, item: Brand) {
        CoroutineScope(Dispatchers.IO).launch {
            productApi.updateBrands(id,item)
            loadBrands() // Обновляем список после изменения
        }
    }

    fun insertBrand(item: Brand) {
        CoroutineScope(Dispatchers.IO).launch {
            productApi.insertBrands(item)
            loadBrands() // Обновляем список после добавления
        }
    }
}