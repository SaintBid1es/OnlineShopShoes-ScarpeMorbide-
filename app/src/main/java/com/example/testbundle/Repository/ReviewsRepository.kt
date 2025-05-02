package com.example.testbundle.Repository

import android.content.Context
import com.example.testbundle.db.Basket
import com.example.testbundle.db.Dao
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import com.example.testbundle.db.Reviews
import kotlinx.coroutines.flow.Flow

class ReviewsRepository private constructor(context: Context)
{

    private var dao: Dao = MainDb.getDb(context).getDao()

    suspend fun deleteReviews(id: Int) {
        dao.deleteReviews(id)
    }
    suspend fun deleteClientItemByProduct(clientID : Int, productID : Int) {
        dao.deleteReviewsByIdClientAndProduct(productID, clientID)
    }
    suspend fun updateReviews(item: Reviews) {
        dao.updateRewiews(item)
    }

    suspend fun insertReviews(item: Reviews) {
        dao.insertReviews(item)
    }
    fun getReviewByProduct(productID: Int) = dao.getReviewsByProduct(productID)
    fun getItems() : Flow<List<Reviews>> = dao.getAllReviews()

    suspend fun getReviews() : Flow<List<Reviews>> = dao.getAllReviews()
    suspend fun getReviewsById(reviews_ID : Int) : Flow<List<Reviews>> = dao.getReviewsByProduct(reviews_ID)

    companion object {
        private var instance: ReviewsRepository? = null

        fun createInstance(context: Context) {
            instance = ReviewsRepository(context)
        }

        fun getInstance(): ReviewsRepository {
            return instance ?: throw NotImplementedError()
        }
    }


}