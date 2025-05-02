package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Dao
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Order
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OrderRepository private constructor(context: Context) {

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
    dao.insertOrder(order)
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