package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import com.example.testbundle.db.Dao
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.Flow

class CategoryRepository private constructor(context: Context) {


    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteCategory(id:Int) {
        dao.deleteCategory(id)
    }

    suspend fun updateCategory(item: Category) {
        dao.updateCategory(item)
    }

    suspend fun insertCategory(item: Category) {
        dao.insertCategory(item)
    }

    fun getCategory() : Flow<List<Category>> = dao.getAllCategory()
    suspend fun getCategoryById(id : Int) : Category? = dao.getCategoryById(id)
    suspend fun getCategoryByName(name : String) : Category? = dao.getCategoryByName(name)
    companion object {
        private var instance: CategoryRepository? = null

        fun createInstance(context: Context) {
            instance = CategoryRepository(context)
        }

        fun getInstance(): CategoryRepository {
            return instance ?: throw NotImplementedError()
        }
    }
}