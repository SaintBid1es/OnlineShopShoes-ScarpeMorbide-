package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Dao
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.Flow

class BasketRepository private constructor(context: Context) {

    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteItem() {
        dao.deleteBasket()
    }
    suspend fun deleteItemById(id:Int) {
        dao.deleteBasketById(id)
    }
    suspend fun deleteClientItemByProduct(clientID : Int, productID : Int,sizeId: Int) {
        dao.deleteBasketById(productID, clientID,sizeId)
    }
    suspend fun getBasketItemByProduct(userId: Int, productId: Int): Basket? {
        return dao.getBasketItemByProduct(userId, productId)
    }



    fun getItemsByUser(userID: Int) =
        dao.getAllBasketByClient(userID)

    suspend fun updateItem(item: Basket) {
        dao.updateBasket(item)
    }

    suspend fun insertItem(item: Basket) {
        dao.insertBasket(item)
    }
    suspend fun getBasketItemByProductAndSize(sizeId: Int, productId: Int,clientID: Int):Int?{
        return dao.getBasketItemByProductAndSize(sizeId, productId,clientID)
    }
        suspend fun isProductInBasket(clientID : Int, productID : Int) : Int?{
            return dao.isProductInBasket(productID, clientID)
        }

    fun getItems(): Flow<List<Basket>> = dao.getAllBasket()

    companion object {
        private var instance: BasketRepository? = null

        fun createInstance(context: Context) {
            instance = BasketRepository(context)
        }

        fun getInstance(): BasketRepository {
            return instance ?: throw NotImplementedError()
        }
    }
}