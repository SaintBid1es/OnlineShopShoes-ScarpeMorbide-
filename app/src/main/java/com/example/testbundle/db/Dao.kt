package com.example.testbundle.db

import android.app.DownloadManager.COLUMN_ID
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID


@Dao
interface Dao {
    @Insert
    suspend fun insertItem(item: Item)
    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<Item>>
    @Query("DELETE FROM items WHERE id = :id_item")
    suspend fun deleteItem(id_item: Int?)
    @Update
    suspend fun updateItem(item: Item)
    @Query("SELECT * FROM items WHERE id = :id_items")
    suspend fun getAccountById(id_items: Int?):Item
    @Query("SELECT * FROM items WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): Item?

    @Insert
    suspend fun insertProduct(item: Products)
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Products>
    @Query("SELECT * FROM products WHERE id= :id_category")
    fun getAllProductsWithCategory(id_category: Int?): Flow<List<Products>>
    @Query("SELECT * FROM products WHERE id= :id_brand")
    fun getAllProductsWithBrand(id_brand: Int?): Flow<List<Products>>
    @Query("DELETE FROM products WHERE id = :id_item")
    suspend fun deleteProduct(id_item: Int?)
    @Update
    suspend fun updateProduct(item: Products)
    @Query("SELECT * FROM products WHERE id = :id_items")
    suspend fun getProductById(id_items: Int?):Products?




    @Insert
    suspend fun insertBrand(item: Brand)
    @Query("SELECT * FROM brand")
    fun getAllBrand(): Flow<List<Brand>>
    @Query("SELECT * FROM brand where nameBrand = :name")
    suspend fun getBrandByName(name : String): Brand?
    @Query("DELETE FROM brand WHERE id = :id_brand")
    suspend fun deleteBrand(id_brand: Int?)
    @Update
    suspend fun updateBrand(item: Brand)
    @Query("SELECT * FROM brand WHERE id = :id_brand")
    suspend fun getBrandById(id_brand: Int?):Brand?
    @Query("SELECT nameBrand FROM brand WHERE id = :brandId")
    suspend fun getBrandNameById(brandId: Int): String?



    @Insert
    suspend fun insertCategory(item: Category)
    @Query("SELECT * FROM category")
    fun getAllCategory(): Flow<List<Category>>
    @Query("DELETE FROM category WHERE id = :id_category")
    suspend fun deleteCategory(id_category: Int?)
    @Update
    suspend fun updateCategory(item: Category)
    @Query("SELECT * FROM category WHERE id = :id_category")
    suspend fun getCategoryById(id_category: Int?):Category
    @Query("SELECT nameCategory FROM category WHERE id = :categoryId")
    suspend fun getCategoryNameById(categoryId: Int): String?
    @Query("SELECT * FROM category where nameCategory = :name")
    suspend fun getCategoryByName(name : String): Category?







    @Insert
    suspend fun insertBasket(item: Basket)
    @Query("SELECT Count(*) FROM basket where product_id =:productId and size=:sizeId and client_id=:clientID")
    suspend fun getBasketItemByProductAndSize(sizeId: Int, productId: Int,clientID:Int): Int?
    @Query("SELECT * FROM basket")
    fun getAllBasket(): Flow<List<Basket>>
    @Query("SELECT * FROM basket where client_id = :clientID")
    fun getAllBasketByClient(clientID : Int): Flow<List<Basket>>
    @Query("DELETE FROM basket WHERE id = :id_basket")
    suspend fun deleteBasketById(id_basket: Int?)
    @Query("DELETE FROM basket WHERE product_id = :productID and client_id = :clientID and size =:sizeID")
    suspend fun deleteBasketById(productID: Int, clientID: Int,sizeID: Int)
    @Query("DELETE FROM basket ")
    suspend fun deleteBasket()
    @Update
    suspend fun updateBasket(item: Basket)
    @Query("SELECT * FROM basket WHERE id = :id_basket")
    suspend fun getBasketById(id_basket: Int?):Basket
    @Query("SELECT Count(*) FROM basket WHERE product_id = :productId and client_id = :userId")
     suspend fun isProductInBasket(productId: Int, userId: Int) : Int?
    @Query("SELECT * FROM basket WHERE client_id = :userId AND product_id = :productId LIMIT 1")
    suspend fun getBasketItemByProduct(userId: Int, productId: Int): Basket?


    @Insert
    suspend fun insertFavorite(item: Favorite)
    @Query("SELECT * FROM favorite")
    fun getAllFavorite(): Flow<List<Favorite>>

    @Query("Select COUNT(id) from favorite where client_id = :clientID and product_id = :productId")
    suspend fun getIsInFavorite(clientID: Int, productId : Int) : Int

    @Query("DELETE FROM favorite WHERE id = :id_favorite")
    suspend fun deleteFavorite(id_favorite: Int?)
    @Update
    suspend fun updateFavorite(item: Favorite)

    @Query("SELECT * FROM favorite WHERE client_id = :id_favorite")
     fun getFavoriteByClient(id_favorite: Int?):Flow<List<Favorite>>

    @Query("DELETE FROM favorite WHERE product_id = :productID and client_id = :clientID")
    suspend fun deleteFavoriteByIdClientAndProduct(productID: Int, clientID: Int)



    @Insert
    suspend fun insertOrder(item: Order)
    @Query("SELECT * FROM `order`")
    fun getAllOrder(): Flow<List<Order>>
    @Query("SELECT * FROM `order` where client_id = :clientID")
    fun getAllOrderByClient(clientID : Int): Flow<List<Order>>
    @Query("DELETE FROM `order` WHERE id = :id_order")
    suspend fun deleteOrderById(id_order: Int?)
    @Query("DELETE FROM `order` ")
    suspend fun deleteOrder()
    @Update
    suspend fun updateOrder(item: Order)
    @Query("SELECT * FROM `order` WHERE id = :id_order")
    suspend fun getOrderById(id_order: Int?):Order
    @Query("SELECT SUM(totalPrice) FROM `order`")
    suspend fun summTotalPrice(): Int
    @Query("SELECT SUM(totalPrice) / COUNT(*) FROM `order`")
    suspend fun avgTotalPrice(): Int




    @Query("SELECT * FROM order_items where orderId = :id ")
    suspend fun getProductOrderItemById(id:UUID):List<OrderItem>
    @Insert
    suspend fun insertOrderItem(item: OrderItem)
    @Query("SELECT * FROM order_items")
    fun getAllOrderItem(): Flow<List<OrderItem>>
    @Query("SELECT * FROM order_items where orderId = :order_id")
    fun getAllOrderItemByOrder(order_id : UUID): Flow<List<OrderItem>>
    @Query("DELETE FROM order_items WHERE id = :id_order")
    suspend fun deleteOrderItemById(id_order: Int?)
    @Query("DELETE FROM order_items ")
    suspend fun deleteOrderItem()
    @Update
    suspend fun updateOrderItem(item: OrderItem)
    @Query("SELECT * FROM order_items WHERE id = :id_order")
    suspend fun getOrderItemById(id_order: Int?):OrderItem


    @Insert
    suspend fun insertReviews(item: Reviews)

    @Query("SELECT * FROM reviews")
     fun getAllReviews(): Flow<List<Reviews>>

    @Query("Select COUNT(id) from reviews where client_id = :clientID and product_id = :productId")
    suspend fun getIsReviews(clientID: Int, productId: Int): Int

    @Query("DELETE FROM reviews WHERE id = :id_rewiews")
    suspend fun deleteReviews(id_rewiews: Int?)

    @Query("SELECT * FROM reviews WHERE id = :id_reviews")
    suspend fun getReviewsById(id_reviews: Int?):Reviews

    @Update
    suspend fun updateRewiews(item: Reviews)
    @Query("SELECT SUM(rating) / COUNT(*) FROM reviews")
    suspend fun avgRating() : Int

    @Query("SELECT * FROM reviews WHERE product_id = :id_product")
    fun getReviewsByProduct(id_product: Int?): Flow<List<Reviews>>

    @Query("DELETE FROM reviews WHERE product_id = :productID and client_id = :clientID")
    suspend fun deleteReviewsByIdClientAndProduct(productID: Int, clientID: Int)

    @Query("SELECT  SUM(rating) from reviews where product_id = :productID ")
    suspend fun selectRating(productID: Int) : Double

    @Query("SELECT Count(*) from reviews where product_id = :productID ")
    suspend fun countRating(productID: Int) : Int


    @Insert
    suspend fun insertImage(image: com.example.testbundle.db.ImageEntity): Long

    @Insert
    suspend fun insertImages(images: List<ImageEntity>): List<Long>

    @Query("SELECT * FROM images WHERE id = :imageId")
    suspend fun getImageById(imageId: Int): ImageEntity?

    @Query("UPDATE products SET imageId = :imageId, imageUri = :imageUri WHERE id = :productId")
    suspend fun updateProductImage(productId: Int, imageId: Int, imageUri: String?)

    @Query("""
    SELECT 'Бренд: ' || brand.nameBrand as label, SUM(order_items.quantity) as total 
    FROM order_items
    JOIN products ON order_items.productId = products.id
    JOIN brand ON products.brandId = brand.id
    GROUP BY brand.nameBrand
    
    UNION ALL
    
    SELECT 'Категория: ' || category.nameCategory as label, SUM(order_items.quantity) as total 
    FROM order_items
    JOIN products ON order_items.productId = products.id
    JOIN category ON products.categoryId = category.id
    GROUP BY category.nameCategory
    
    ORDER BY total DESC
""")
    suspend fun getCombinedBrandCategoryStats(): List<CombinedStat>

    data class CombinedStat(val label: String, val total: Int)

    @Query("SELECT * FROM products WHERE id IN (:ids)")
    suspend fun getProductsByIds(ids: List<Int>): List<Products>

    @Query("SELECT * FROM brand WHERE id IN (:ids)")
    suspend fun getBrandsByIds(ids: List<Int>): List<Brand>

    @Query("SELECT * FROM category WHERE id IN (:ids)")
    suspend fun getCategoriesByIds(ids: List<Int>): List<Category>

    @Query("""
    SELECT SUM(totalPrice) 
    FROM `order` 
    WHERE date(orderDate) = date('now')
""")
    suspend fun getDailyRevenue(): Double?

    @Query("""
    SELECT SUM(totalPrice) 
    FROM `order` 
    WHERE strftime('%Y-%m', orderDate) = strftime('%Y-%m', 'now')
""")
    suspend fun getMonthlyRevenue(): Double?

    @Query("""
    SELECT SUM(totalPrice) 
    FROM `order` 
    WHERE strftime('%Y', orderDate) = strftime('%Y', 'now')
""")
    suspend fun getYearlyRevenue(): Double?
}