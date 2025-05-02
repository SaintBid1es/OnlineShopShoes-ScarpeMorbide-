package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Dao
import com.example.testbundle.db.Item
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.Flow

class ItemsRepository private constructor(
    context: Context
) {
    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteItem(id: Int) {
        dao.deleteItem(id)
    }


    suspend fun updateItem(item: Item) {
        dao.updateItem(item)
    }

    suspend fun insertItem(item: Item) {
        dao.insertItem(item)
    }

    fun getItems() : Flow<List<Item>> = dao.getAllItems()

    companion object {
        private var instance: ItemsRepository? = null

        fun createInstance(context: Context) {
            instance = ItemsRepository(context)
        }

        fun getInstance(): ItemsRepository {
            return instance ?: throw NotImplementedError()
        }
    }
}