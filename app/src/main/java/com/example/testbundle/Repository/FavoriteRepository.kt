package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Dao
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.Flow

class FavoriteRepository private constructor(context: Context)
{

    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteFavorite(id: Int) {
        dao.deleteFavorite(id)
    }
    suspend fun deleteClientItemByProduct(clientID : Int, productID : Int) {
        dao.deleteFavoriteByIdClientAndProduct(productID, clientID)
    }
    suspend fun updateFavorite(item: Favorite) {
        dao.updateFavorite(item)
    }

    suspend fun insertFavorite(item: Favorite) {
        dao.insertFavorite(item)
    }
    fun getItemsByUser(userID: Int) =
        dao.getFavoriteByClient(userID)
    fun getItems() : Flow<List<Favorite>> = dao.getAllFavorite()

    suspend fun getIsInFavorite(clientID : Int, productID : Int) : Int = dao.getIsInFavorite(clientID, productID)

    companion object {
        private var instance: FavoriteRepository? = null

        fun createInstance(context: Context) {
            instance = FavoriteRepository(context)
        }

        fun getInstance(): FavoriteRepository {
            return instance ?: throw NotImplementedError()
        }
    }


}