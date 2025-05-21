package com.example.testbundle.API

import com.example.testbundle.db.Item
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiService {
    @GET("users")
    fun getUsers(): Call<MutableList<Item?>?>?

    @POST("users")
    fun createUser(@Body user: Item?): Call<Item?>?
}