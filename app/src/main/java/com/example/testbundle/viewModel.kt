package com.example.testbundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.testbundle.Repository.ItemsRepository
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
) : ViewModel() {
    /**
     * Функция удаления пользователя по идентификатору
     */
    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repo.deleteItem(id)
        }
    }

    /**
     * Функция обновления пользователя
     */
    fun updateItem(item: Item) {
        viewModelScope.launch {
            repo.updateItem(item)
        }
    }

    /**
     * Функция добавления пользователя
     */
    fun insertItem(item: Item){
        viewModelScope.launch {
            repo.insertItem(item)
        }
    }




    private val repo = ItemsRepository.getInstance()

    private val _state: MutableStateFlow<List<Item>> = MutableStateFlow(emptyList())
    val state: StateFlow<List<Item>>
        get() = _state.asStateFlow()

    /**
     * Инициализация списка
     */
    init {
        viewModelScope.launch {
            repo.getItems().collect { list ->
                _state.update {
                    list
                }
            }
        }
    }

}