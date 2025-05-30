package com.example.testbundle.API

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
//    private const val BASE_URL = "http://192.168.1.6:5008/api/" //HOME
    private const val BASE_URL = "http://192.168.0.74:5008/api/" //RABOTA

    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}