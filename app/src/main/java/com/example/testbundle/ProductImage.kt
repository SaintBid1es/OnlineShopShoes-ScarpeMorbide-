package com.example.testbundle

import android.net.Uri

sealed class ProductImage {
    data class DrawableImage(val resId: Int) : ProductImage()
    data class UriImage(val uri: Uri) : ProductImage()
}