package com.example.testbundle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "brandFilter",
    foreignKeys =
    [ForeignKey(
        entity = Brand::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("brand_id"),
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Products::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("product_id"),
        onDelete = ForeignKey.CASCADE
    )])
data class BrandFilter(
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null,
    @ColumnInfo(name = "brand_id")
    var brandId:Int,
    @ColumnInfo(name = "product_id")
    var productId:Int
)
