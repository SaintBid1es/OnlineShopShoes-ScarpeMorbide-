package com.example.testbundle.API

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


 object RetrofitClient {
     const val BASE_URL = "http://192.168.1.6:5008/api/" //HOME
//     const val BASE_URL = "http://192.168.0.74:5008/api/" //RABOTA
   //  const val BASE_URL = "https://fd51-185-77-216-6.ngrok-free.app/api/" //NGROK

    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()

                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}