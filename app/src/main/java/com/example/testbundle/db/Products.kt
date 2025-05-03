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
    @ColumnInfo(name = "amount")
    var amount:Int,
    val imageUri: String?, // Для URI

)
data class ProductsModel(
    val id:Int? = null,
    val name:String,
    val description: String,
    val cost:Double,
    val imageId:Int,
    val isFavorite : Boolean,
    val imageUri: String? = null, // Для URI
)
data class BasketModel(
    val id:Int? = null,
    val name:String,
    val description: String,
    val cost:Double,
    val imageId:Int,
    val count:Int,
    val size:Int,
    val amount: Int,
    var brand:Int,
    var category:Int,

    )
@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "image_data")
    val imageData: ByteArray
)