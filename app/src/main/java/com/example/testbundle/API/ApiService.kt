package com.example.testbundle.API

//import com.example.testbundle.Repository.LoginRequest
//import com.example.testbundle.Repository.LoginResponse
import com.example.testbundle.Repository.LoginRequest
import com.example.testbundle.Repository.LoginResponse
import com.example.testbundle.Repository.RefreshTokenRequest
import com.example.testbundle.Repository.RefreshTokenResponse

import com.example.testbundle.db.Basket
import com.example.testbundle.db.Brand
import com.example.testbundle.db.BrandFilter
import com.example.testbundle.db.Category
import com.example.testbundle.db.CategoryFilter
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.ImageEntity
import com.example.testbundle.db.Item
import com.example.testbundle.db.Order
import com.example.testbundle.db.OrderItem
import com.example.testbundle.db.Products
import com.example.testbundle.db.Reviews
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface ApiService {
    // USERS
//    @GET("Users")
//    suspend fun getUsers(): List<Item>
//
    @GET("Users")
    suspend fun getUsers(@Header("Authorization") token: String): List<Item>

    @GET("Users/{id}")
    suspend fun getUsersByID(@Path("id") id: Int,@Header("Authorization") token: String): Item

    @GET("Users/GetUserByEmail/{email}")
    suspend fun getUsersByEmail(@Path("email") email: String): Item?

    @POST("Users")
    suspend fun insertUser(@Body user: Item): Item

    @POST("Users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("Users/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @PUT("Users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: Item): Response<Unit>

    @DELETE("Users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    //IMAGE
    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<ImageUploadResponse>
    data class ImageUploadResponse(val url: String)

    // BASKETS
    @GET("Baskets")
    suspend fun getBaskets(): List<Basket>

    @GET("Baskets/{id}")
    suspend fun getBasketsByID(@Path("id") id: Int): Basket

    @GET("Baskets/getBasketItemByProduct/{userId}/{productId}")
    suspend fun getBasketItemByProduct(@Path("userId") userId: Int, @Path("productId") productID: Int) : Basket?

    @GET("Baskets/getBasketItemByProductAndSize/{userId}/{productId}/{sizeId}")
    suspend fun getBasketItemByProductAndSize(@Path("userId") userId: Int, @Path("productId") productID: Int,@Path("sizeId") sizeId: Int) : Int?

    @GET("Baskets/GetBasketByUser/{userId}")
    suspend fun getItemsByUser(@Path("userId") userId: Int): List<Basket>

    @POST("Baskets")
    suspend fun insertBasket(@Body basket: Basket): Basket

    @PUT("Baskets/{id}")
    suspend fun updateBaskets(@Path("id") id: Int, @Body basket: Basket): Response<Unit>

    @DELETE("Baskets/{id}")
    suspend fun deleteBaskets(@Path("id") id: Int): Response<Unit>

    @DELETE("Baskets/Reset")
    suspend fun deleteTableBaskets(): Response<Unit>

    @DELETE("Baskets/{product_id}/{clientID}/{sizeID}")
    suspend fun deleteClientItemByProductBasket(@Path("product_id") product_id: Int,@Path("clientID") clientID: Int,@Path("sizeID") sizeID: Int)

    // BRANDFILTERS
    @GET("Brandfilters")
    suspend fun getBrandfilters(): List<BrandFilter>

    @GET("Brandfilters/{id}")
    suspend fun getBrandfiltersByID(@Path("id") id: Int): BrandFilter

    @POST("Brandfilters")
    suspend fun insertBrandfilters(@Body brandFilter: BrandFilter): BrandFilter

    @PUT("Brandfilters/{id}")
    suspend fun updateBrandfilters(@Path("id") id: Int, @Body brandFilter: BrandFilter): Response<Unit>

    @DELETE("Brandfilters/{id}")
    suspend fun deleteBrandfilters(@Path("id") id: Int): Response<Unit>

    @DELETE("Brandfilters/Reset")
    suspend fun deleteTableBrandfilters(): Response<Unit>

    // BRANDS
    @GET("Brands")
    suspend fun getBrands(): List<Brand>

    @GET("Brands/{id}")
    suspend fun getBrandsByID(@Path("id") id: Int): Brand

    @GET("Brands/GetBrandByName/{nameBrand}")
    suspend fun getBrandByName(@Path("nameBrand") nameBrand: String): Brand?

    @GET("Brands/GetProductsByIds/{ids}")
    suspend fun GetBrandsByIds(@Path("ids") ids: List<Int>): List<Brand>

    @GET("Brands/getBrandNameById/{brandId}")
    suspend fun getBrandNameById(@Path("brandId") brandId: Int): String

    @POST("Brands")
    suspend fun insertBrands(@Body brand: Brand): Brand

    @PUT("Brands/{id}")
    suspend fun updateBrands(@Path("id") id: Int, @Body brand: Brand): Response<Unit>

    @DELETE("Brands/{id}")
    suspend fun deleteBrands(@Path("id") id: Int): Response<Unit>

    @DELETE("Brands/Reset")
    suspend fun deleteTableBrands(): Response<Unit>

    // CATEGORIES
    @GET("Categories")
    suspend fun getCategories(): List<Category>

    @GET("Categories/{id}")
    suspend fun getCategoriesByID(@Path("id") id: Int): Category

    @GET("Categories/getCategoryNameById/{categoryId}")
    suspend fun getCategoryNameById(@Path("categoryId") categoryId: Int): String

    @GET("Categories/GetCategoryByName/{nameCategory}")
    suspend fun getCategoryByName(@Path("nameCategory") nameCategory: String): Category?

    @GET("Categories/GetCategoryByIds/{ids}")
    suspend fun getCategoryByIds(@Path("ids") ids: List<Int>): List<Category>

    @POST("Categories")
    suspend fun insertCategories(@Body category: Category): Category

    @PUT("Categories/{id}")
    suspend fun updateCategories(@Path("id") id: Int, @Body category: Category): Response<Unit>

    @DELETE("Categories/{id}")
    suspend fun deleteCategories(@Path("id") id: Int): Response<Unit>

    @DELETE("Categories/Reset")
    suspend fun deleteTableCategories(): Response<Unit>

    // CATEGORYFILTERS
    @GET("Categoryfilters")
    suspend fun getCategoryfilters(): List<CategoryFilter>

    @GET("Categoryfilters/{id}")
    suspend fun getCategoryfiltersByID(@Path("id") id: Int): CategoryFilter

    @POST("Categoryfilters")
    suspend fun insertCategoryfilters(@Body categoryFilter: CategoryFilter): CategoryFilter

    @PUT("Categoryfilters/{id}")
    suspend fun updateCategoryfilters(@Path("id") id: Int, @Body categoryFilter: CategoryFilter): Response<Unit>

    @DELETE("Categoryfilters/{id}")
    suspend fun deleteCategoryfilters(@Path("id") id: Int): Response<Unit>

    @DELETE("Categoryfilters/Reset")
    suspend fun deleteTableCategoryfilters(): Response<Unit>

    // IMAGES
    @GET("Images")
    suspend fun getImages(): List<ImageEntity>

    @GET("Images/{id}")
    suspend fun getImagesByID(@Path("id") id: Int): ImageEntity

    @POST("Images")
    suspend fun insertImages(@Body imageEntity: ImageEntity): ImageEntity

    @PUT("Images/{id}")
    suspend fun updateImages(@Path("id") id: Int, @Body imageEntity: ImageEntity): Response<Unit>

    @DELETE("Images/{id}")
    suspend fun deleteImages(@Path("id") id: Int): Response<Unit>

    @DELETE("Images/Reset")
    suspend fun deleteTableImages(): Response<Unit>

    // ORDERITEMS
    @GET("OrderItems")
    suspend fun getOrderItems(): List<OrderItem>

    @GET("OrderItems/{id}")
    suspend fun getOrderItemsByID(@Path("id") id: Int): OrderItem

    @GET("OrderItems/GetProductOrderItemById/{orderId}")
    suspend fun getProductOrderItemById(@Path("orderId") orderId: UUID): List<OrderItem>

    @POST("OrderItems")
    suspend fun insertOrderItems(@Body orderItem: OrderItem): OrderItem

    @PUT("OrderItems/{id}")
    suspend fun updateOrderItems(@Path("id") id: Int, @Body orderItem: OrderItem): Response<Unit>

    @DELETE("OrderItems/{id}")
    suspend fun deleteOrderItems(@Path("id") id: Int): Response<Unit>

    @DELETE("OrderItems/Reset")
    suspend fun deleteTableOrderItems(): Response<Unit>

    // ORDERS
    @GET("Orders")
    suspend fun getOrders(): List<Order>

    @GET("Orders/avgTotalPrice")
    suspend fun avgTotalPrice(): Int

    @GET("Orders/summTotalPrice")
    suspend fun summTotalPrice(): Int

    @GET("Orders/getDailyRevenue")
    suspend fun getDailyRevenue(): Double

    @GET("Orders/getMonthlyRevenue")
    suspend fun getMonthlyRevenue(): Double

    @GET("Orders/getYearlyRevenue")
    suspend fun getYearlyRevenue(): Double

    @GET("Orders/{id}")
    suspend fun getOrdersByID(@Path("id") id: Int): Order

    @GET("Orders/GetOrderByClient/{clientId}")
    suspend fun getOrderByClient(@Path("clientId") clientId: Int): List<Order>

    @POST("Orders")
    suspend fun insertOrders(@Body order: Order): Order

    @PUT("Orders/{id}")
    suspend fun updateOrders(@Path("id") id: Int, @Body order: Order): Response<Unit>

    @DELETE("Orders/{id}")
    suspend fun deleteOrders(@Path("id") id: Int): Response<Unit>

    @DELETE("Orders/Reset")
    suspend fun deleteTableOrders(): Response<Unit>

    // PRODUCTS
    @GET("Products")
    suspend fun getProducts(): List<Products>

    @GET("Products/{id}")
    suspend fun getProductsByID(@Path("id") id: Int): Products

    @GET("Products/GetProductsByIds/{ids}")
    suspend fun GetProductsByIds(@Path("ids") ids: List<Int>): List<Products>

    @POST("Products")
    suspend fun insertProducts(@Body products: Products): Products

    @PUT("Products/{id}")
    suspend fun updateProducts(@Path("id") id: Int, @Body products: Products): Response<Unit>

    @PUT("Products/updateProductImage/{productId}/{imageId}/{imageUri}")
    suspend fun updateProductImage(@Path("id") productId: Int, @Path("imageId") imageId: Int, @Path("imageUri") imageUri: String?, @Body products: Products): Response<Unit>

    @DELETE("Products/{id}")
    suspend fun deleteProducts(@Path("id") id: Int): Response<Unit>

    @DELETE("Products/Reset")
    suspend fun deleteTableProducts(): Response<Unit>

    // REVIEWS
    @GET("Reviews")
    suspend fun getReviews(): List<Reviews>

    @GET("Reviews/avgRating")
    suspend fun avgRating(): Int

    @GET("Reviews/{id}")
    suspend fun getReviewsByID(@Path("id") id: Int): Reviews

    @GET("Reviews/GetReviewByProductId/{productId}")
    suspend fun getReviewByProductId(@Path("productId") productId: Int): List<Reviews>

    @GET("Reviews/selectRating/{product_id}")
    suspend fun selectRating(@Path("product_id") productID:Int) : Double

    @GET("Reviews/countRating/{product_id}")
    suspend fun countRating(@Path("product_id") productID:Int) : Int

    @POST("Reviews")
    suspend fun insertReviews(@Body reviews: Reviews): Reviews

    @PUT("Reviews/{id}")
    suspend fun updateReviews(@Path("id") id: Int, @Body reviews: Reviews): Response<Unit>

    @DELETE("Reviews/{id}")
    suspend fun deleteReviews(@Path("id") id: Int): Response<Unit>

    @DELETE("Reviews/Reset")
    suspend fun deleteTableReviews(): Response<Unit>

    // FAVORITES
    @GET("Favorites")
    suspend fun getFavorites(): List<Favorite>

    @GET("Favorites/getFavoriteByClient/{clientId}")
    suspend fun getFavoritesByID(@Path("clientId") clientId: Int): List<Favorite>

    @GET("Favorites/getIsInFavorite/{userId}/{productId}")
    suspend fun getIsInFavorite(@Path("userId") userId: Int,@Path("productId") productID: Int): Int

    @POST("Favorites")
    suspend fun insertFavorites(@Body favorite: Favorite): Favorite

    @PUT("Favorites/{id}")
    suspend fun updateFavorites(@Path("id") id: Int, @Body favorite: Favorite): Response<Unit>

    @DELETE("Favorites/{id}")
    suspend fun deleteFavorites(@Path("id") id: Int): Response<Unit>

    @DELETE("Favorites/deleteClientItemByProductFavorite/{clientId}/productId")
    suspend fun deleteClientItemByProductFavorite(@Path("clientId") userId: Int,@Path("productId") productID: Int): Response<Unit>

    @DELETE("Favorites/deleteFavoriteByIdClientAndProduct/{productId}/{clientId}")
    suspend fun deleteFavoriteByIdClientAndProduct(@Path("productId") productId: Int,@Path("clientId") clientID: Int): Response<Unit>

    @DELETE("Favorites/Reset")
    suspend fun deleteTableFavorites(): Response<Unit>
}