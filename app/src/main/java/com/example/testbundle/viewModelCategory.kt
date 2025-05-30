package com.example.testbundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.Repository.CategoryRepository
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
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

class CategoryViewModel : ViewModel() {

    private val productApi = RetrofitClient.apiService

    private val _stateCategory = MutableStateFlow<List<Category>>(emptyList())
    val stateCategory: StateFlow<List<Category>> = _stateCategory.asStateFlow()

    init {
        loadCategories() // Загружаем категории при инициализации
    }

    private fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val categories = productApi.getCategories()
                _stateCategory.update { categories }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productApi.deleteCategories(id)
                loadCategories() // Обновляем список после удаления
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun checkAndInsertCategory(name: String, onExists: () -> Unit, onSuccess: (Category) -> Unit) {
        viewModelScope.launch {
            val categories = productApi.getCategories()
            val categoryExists = categories.any { it.namecategory == name }

            if (categoryExists) {
                onExists()
            } else {
                val newCategory = Category(null, name)
                insertCategory(newCategory)
                onSuccess(newCategory)
            }
        }
    }

    fun updateCategory(id: Int, item: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productApi.updateCategories(id, item)
                loadCategories() // Обновляем список после изменения
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun insertCategory(item: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productApi.insertCategories(item)
                loadCategories() // Обновляем список после добавления
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}