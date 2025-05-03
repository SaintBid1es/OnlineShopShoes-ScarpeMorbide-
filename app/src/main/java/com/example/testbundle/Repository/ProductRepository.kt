package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Dao
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import kotlinx.coroutines.flow.Flow

class ProductRepository private constructor(
    context: Context
){
    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteProduct(id: Int) {
        dao.deleteProduct(id)
    }

    suspend fun updateProduct(item: Products) {
        dao.updateProduct(item)
    }

    suspend fun insertProduct(item: Products) {
        dao.insertProduct(item)
    }
    suspend fun filterCategory(id:Int){
        dao.getAllProductsWithCategory(id)
    }
    suspend fun filterBrand(id:Int){
        dao.getAllProductsWithBrand(id)
    }
   suspend fun countRating(id:Int):Int{
        return  dao.countRating(id)
    }
  suspend fun selectRating(id:Int):Double{
        return dao.selectRating(id)
    }

    suspend fun getProducts() : List<Products> = dao.getAllProducts()
    suspend fun getProductById(productID : Int) : Products? = dao.getProductById(productID)

    companion object {
        private var instance: ProductRepository? = null

        fun createInstance(context: Context) {
            instance = ProductRepository(context)
        }

        fun getInstance(): ProductRepository {
            return instance ?: throw NotImplementedError()
        }
    }
}