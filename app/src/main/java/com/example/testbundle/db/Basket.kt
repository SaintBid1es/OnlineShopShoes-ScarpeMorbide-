package com.example.testbundle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "basket",


    foreignKeys =
    [ForeignKey(
        entity = Item::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("clientId"),
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Products::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("productId"),
        onDelete = ForeignKey.CASCADE
    )])
data class Basket(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo
    var clientId: Int,
    @ColumnInfo
    var productId: Int,
    @ColumnInfo(name = "countBasket")
    var countbasket: Int,
    @ColumnInfo(name = "size")
    var size: Int,

)
