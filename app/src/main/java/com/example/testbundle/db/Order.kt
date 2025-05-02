package com.example.testbundle.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "order",
    foreignKeys =
    [ForeignKey(
        entity = Item::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("client_id"),
        onDelete = ForeignKey.CASCADE
    )])
data class Order(
    @PrimaryKey
    val id:UUID = UUID.randomUUID(),
    @ColumnInfo(name = "client_id")
    var client_id:Int,
    @ColumnInfo(name = "orderDate")
    var orderDate:String,
    @ColumnInfo(name = "totalPrice")
    var totalPrice:Double,
)
data class OrderModel(
    val id:UUID,
    var orderDate:String,
    var totalPrice:Double,
    var products:String,

)