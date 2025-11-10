package com.example.huertohogar.data

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String, // URL de la imagen
    val stock: Int,
    val category: String
)
data class CartItem(
    val product: Product,
    var quantity: Int
)
data class User(
    val email: String,
    val uid: String
)
data class Sale(
    val userId: String,
    val items: List<CartItem>,
    val total: Double,
    val timestamp: Long
)
