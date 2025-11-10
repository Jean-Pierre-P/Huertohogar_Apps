package com.example.huertohogar.data
import androidx.annotation.DrawableRes

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = ""
)

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val stock: Int = 0,
    val category: String = ""
)

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class Sale(
    val id: String = "",
    val userId: String,
    val items: List<CartItem>,
    val total: Double,
    val timestamp: Long
)