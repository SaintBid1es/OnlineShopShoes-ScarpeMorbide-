package com.example.testbundle.db

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "products",foreignKeys =
[ForeignKey(
    entity = Brand::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("brandId"),
    onDelete = ForeignKey.CASCADE
), ForeignKey(
    entity = Category::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("categoryId"),
    onDelete = ForeignKey.CASCADE
)])
data class Products(
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null,
    @ColumnInfo(name = "name")
    var name:String,
    @ColumnInfo(name = "cost")
    var cost:Double,
    @ColumnInfo(name = "description")
    var description:String,
    @ColumnInfo(name = "size")
    var size :Int?,
    @ColumnInfo(name = "imageId")
    var imageId:Int,
    @ColumnInfo(name = "brandId")
    var brandId:Int,
    @ColumnInfo(name = "categoryId")
    var category:Int,

)
data class ProductsModel(
    val id:Int? = null,
    val name:String,
    val description: String,
    val cost:Double,
    val imageId:Int,
    val isFavorite : Boolean,

)
data class BasketModel(
    val id:Int? = null,
    val name:String,
    val description: String,
    val cost:Double,
    val imageId:Int,
    val count:Int,
    val size:Int

)