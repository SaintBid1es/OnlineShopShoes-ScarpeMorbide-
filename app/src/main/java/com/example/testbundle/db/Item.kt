package com.example.testbundle.db

import android.widget.Spinner
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "items")
data class Item (
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null,
    @ColumnInfo(name = "password")
    var password:String,
    @ColumnInfo(name = "name")
    var name:String,
    @ColumnInfo(name = "surname")
    var surname:String,
    @ColumnInfo(name = "email")
    var email:String,
    @ColumnInfo(name = "telephone")
    var telephone:String,
    @ColumnInfo(name = "speciality")
    var speciality: String,
    @ColumnInfo(name = "avatar")
    var avatar: String?

)