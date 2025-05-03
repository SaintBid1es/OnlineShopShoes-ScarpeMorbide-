package com.example.testbundle.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database (entities =
[Item::class, Products::class, Basket::class, Brand::class, Category::class,
    Favorite::class, CategoryFilter::class, BrandFilter::class, Order::class,OrderItem::class,Reviews::class,ImageEntity::class
    ], version = 20
)


abstract class MainDb:RoomDatabase() {

    abstract fun getDao(): Dao

    companion object{
        fun getDb(context: Context): MainDb {
            return databaseBuilder(context, MainDb::class.java, "database")
                .addMigrations(MainDb.MIGRATION_1_2)
                .allowMainThreadQueries()
                .build();
        }
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Employee ADD COLUMN birthday INTEGER DEFAULT 0 NOT NULL")
            }
        }
    }


}