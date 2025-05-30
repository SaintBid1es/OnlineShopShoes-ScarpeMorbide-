package com.example.testbundle.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "order_items",
    foreignKeys = [ForeignKey(
        entity = Order::class,
        parentColumns = ["id"],
        childColumns = ["orderid"],
        onDelete = ForeignKey.CASCADE
    ),ForeignKey(
        entity = Products::class,
        parentColumns = ["id"],
        childColumns = ["productid"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var orderid: UUID,
    var productid: Int,
    var productname: String,
    var quantity: Int,
    var price: Double,
    var size:Int
)
data class SalesData(
    val name: String,
    val quantity: Int,
    val type: String // "product", "brand", или "category"
)