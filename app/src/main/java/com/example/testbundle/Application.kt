package com.example.testbundle

import android.app.Application
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Repository.BasketRepository
import com.example.testbundle.Repository.BrandRepository
import com.example.testbundle.Repository.CategoryRepository
import com.example.testbundle.Repository.FavoriteRepository
import com.example.testbundle.Repository.ItemsRepository
import com.example.testbundle.Repository.OrderItemsRepository
import com.example.testbundle.Repository.OrderRepository
import com.example.testbundle.Repository.ProductRepository
import com.example.testbundle.Repository.ReviewsRepository

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        ItemsRepository.createInstance(applicationContext)
        ProductRepository.createInstance(applicationContext)
        FavoriteRepository.createInstance(applicationContext)
        BasketRepository.createInstance(applicationContext)
        BrandRepository.createInstance(applicationContext)
        OrderRepository.createInstance(applicationContext)
        OrderItemsRepository.createInstance(applicationContext)
        BrandRepository.createInstance(applicationContext)
        CategoryRepository.createInstance(applicationContext)
        DataStoreRepo.createInstance(applicationContext)
        ReviewsRepository.createInstance(applicationContext)
    }
}