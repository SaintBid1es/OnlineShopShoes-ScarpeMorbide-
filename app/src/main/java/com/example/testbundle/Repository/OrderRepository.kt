package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.API.ApiService
import com.example.testbundle.db.Dao
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Order
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class OrderRepository private constructor(context: Context) {
    private val productApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.6:5008/api/")
//            .baseUrl("http://192.168.0.74:5008/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteItem() {
        dao.deleteOrder()
    }
    suspend fun deleteOrderById(id:Int) {
        dao.deleteOrderById(id)
    }


    fun getOrdersByUser(userID: Int) =
        dao.getAllOrderByClient(userID)

    suspend fun updateItem(item: Order) {
        dao.updateOrder(item)
    }


suspend fun insertItem(order: Order): UUID {
    productApi.insertOrders(order)
    return order.id
}


    fun getItems(): Flow<List<Order>> = dao.getAllOrder()

    companion object {
        private var instance: OrderRepository? = null

        fun createInstance(context: Context) {
            instance = OrderRepository(context)
        }

        fun getInstance(): OrderRepository {
            return instance ?: throw NotImplementedError()
        }
    }
}