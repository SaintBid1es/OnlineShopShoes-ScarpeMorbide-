package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.API.ApiService
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import com.example.testbundle.db.Dao
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CategoryRepository private constructor(context: Context) {
    private var dao: Dao = MainDb.getDb(context).getDao()

    private val productApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.6:5008/api/")
//            .baseUrl("http://192.168.0.74:5008/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    suspend fun deleteCategory(id:Int) {
        productApi.deleteCategories(id)
    }



    suspend fun updateCategory(id: Int, item: Category) {
        productApi.updateCategories(id,item)
    }

    suspend fun insertCategory(item: Category) {
        productApi.insertCategories(item)
    }

//    fun getCategory() : Flow<List<Category>> = dao.getAllCategory()
    suspend fun getCategoryById(id : Int) : Category? = productApi.getCategoriesByID(id)
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