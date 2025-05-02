package com.example.testbundle

import android.content.Context

class FavoritePreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun setFavorite(productId: Int, isFavorite: Boolean) {
        sharedPreferences.edit().putBoolean("product_$productId", isFavorite).apply()
    }

    fun isFavorite(productId: Int): Boolean {
        return sharedPreferences.getBoolean("product_$productId", false)
    }
}