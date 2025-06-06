package com.example.testbundle

import android.net.Uri

sealed class ProductImage {
    data class DrawableImage(val resId: Int) : ProductImage()
    data class UrlImage(val url: String) : ProductImage()
}