package com.example.testbundle.db

import android.media.Rating
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews", foreignKeys =
    [ForeignKey(
        entity = Item::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("client_id"),
        onDelete = ForeignKey.CASCADE
    ),ForeignKey(
        entity = Products::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("product_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Reviews(
    @PrimaryKey
    var id: Int? = null,
    @ColumnInfo(name = "heading")
    var heading:String,
    @ColumnInfo(name = "description")
    var description:String,
    @ColumnInfo(name = "reviewDate")
    var Reviewdate:String,
    @ColumnInfo(name = "rating")
    var rating: Double,
    @ColumnInfo(name = "client_id")
    var client_id: Int,
    @ColumnInfo(name = "product_id")
    var product_id: Int
)
