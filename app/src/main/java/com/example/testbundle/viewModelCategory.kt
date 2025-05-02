package com.example.testbundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.Repository.CategoryRepository
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(
) : ViewModel() {






    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repos.deleteCategory(id)
        }
    }
    fun updateCategory(item: Category) {
        viewModelScope.launch {
            repos.updateCategory(item)
        }
    }
    fun insertCategory(item: Category){
        viewModelScope.launch {
            repos.insertCategory(item)
        }
    }

    private val repos = CategoryRepository.getInstance()

    private val _stateCategory: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())
    val stateCategory: StateFlow<List<Category>>
        get() = _stateCategory.asStateFlow()

    init {
        viewModelScope.launch {
            repos.getCategory().collect { list ->
                _stateCategory.update {
                    list
                }
            }
        }
    }

}