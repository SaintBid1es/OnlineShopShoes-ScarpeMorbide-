package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Dao
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Order
import com.example.testbundle.db.OrderItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OrderItemsRepository private constructor(context: Context) {

    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteItem() {
        dao.deleteOrderItem()
    }
    suspend fun deleteOrderItemById(id:Int) {
        dao.deleteOrderItemById(id)
    }


    fun getItemsByUser(userID: UUID) =
        dao.getAllOrderItemByOrder(userID)

    suspend fun updateItem(item: OrderItem) {
        dao.updateOrderItem(item)
    }

    suspend fun insertItem(item: OrderItem) {
        dao.insertOrderItem(item)
    }

  suspend fun getProductsByOrderId(id_order:UUID):List<OrderItem>{
      return  dao.getProductOrderItemById(id_order)
  }
    fun getItems(): Flow<List<OrderItem>> = dao.getAllOrderItem()

    companion object {
        private var instance: OrderItemsRepository? = null

        fun createInstance(context: Context) {
            instance = OrderItemsRepository(context)
        }

        fun getInstance(): OrderItemsRepository {
            return instance ?: throw NotImplementedError()
        }
    }
}