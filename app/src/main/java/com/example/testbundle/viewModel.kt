package com.example.testbundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient

import com.example.testbundle.Repository.AuthRepository
import com.example.testbundle.Repository.ItemsRepository
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel(
) : ViewModel() {

    private val productApi = RetrofitClient.apiService
    private var authRepository = AuthRepository.getInstance()

    /**
     * Функция удаления пользователя по идентификатору
     */
    fun deleteItem(id: Int,token: String) {
        viewModelScope.launch {

            productApi.deleteUser(id,token)
            loadUsers()
        }
    }
    suspend fun checkEmailExists(email: String): Boolean {
        val token = authRepository.getRefreshToken()!!
        return productApi.getUsers(token).any { it.email == email }
    }

    suspend fun createUser(
        email: String,
        password: String,
        name: String,
        surname: String,
        telephone: String,
        specialty: String,

    ) {
        val item = Item(
            null,
            password,
            name,
            surname,
            email,
            telephone,
            specialty,
            null
        )
        productApi.insertUser(item)
    }

    /**
     * Функция обновления пользователя
     */
    fun updateItem(id:Int,item: Item) {
        viewModelScope.launch {
            productApi.updateUser(id,item)
            loadUsers()
        }
    }

    /**
     * Функция добавления пользователя
     */
    fun insertItem(item: Item){
        viewModelScope.launch {
            productApi.insertUser(item)
            loadUsers()
        }

    }


    fun loadUsers(){
        viewModelScope.launch {
            val token = authRepository.getRefreshToken()!!
            val users = productApi.getUsers(token)
            _state.update {
                users
            }
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
       loadUsers()
    }

}