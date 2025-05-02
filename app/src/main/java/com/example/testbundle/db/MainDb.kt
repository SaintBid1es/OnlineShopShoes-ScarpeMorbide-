package com.example.testbundle.db

import android.content.Context
import androidx.room.Database
import androidx.room.Index
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase



@Database (entities =
[Item::class, Products::class, Basket::class, Brand::class, Category::class,
    Favorite::class, CategoryFilter::class, BrandFilter::class, Order::class,OrderItem::class,Reviews::class
    ], version = 19
)


abstract class MainDb:RoomDatabase() {

    abstract fun getDao(): Dao

    companion object{
        fun getDb(context: Context): MainDb {
            return Room.databaseBuilder(context, MainDb::class.java, "test.db")
                .fallbackToDestructiveMigration()
                .build();
        }
    }


}