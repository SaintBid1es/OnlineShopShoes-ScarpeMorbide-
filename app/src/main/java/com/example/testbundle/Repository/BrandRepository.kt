package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category
import com.example.testbundle.db.Dao
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.flow.Flow

class BrandRepository private constructor(context: Context) {


    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteBrand(id:Int) {
        dao.deleteBrand(id)
    }

    suspend fun updateBrand(item: Brand) {
        dao.updateBrand(item)
    }

    suspend fun insertBrand(item: Brand) {
        dao.insertBrand(item)
    }

    fun getBrand() : Flow<List<Brand>> = dao.getAllBrand()
    suspend fun getBrandById(id : Int) : Brand? = dao.getBrandById(id)
    suspend fun getBrandByName(name : String) : Brand? = dao.getBrandByName(name)



    companion object {
        private var instance: BrandRepository? = null

        fun createInstance(context: Context) {
            instance = BrandRepository(context)
        }

        fun getInstance(): BrandRepository {
            return instance ?: throw NotImplementedError()
        }
    }
}