package com.example.huertohogar.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar.data.CartItem
import com.example.huertohogar.data.Product
import com.example.huertohogar.data.Sale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isNotEmpty() && pass.isNotEmpty()) {
            Log.d("AuthViewModel", "Simulando login exitoso para: $email")
            _isLoggedIn.value = true
        }
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}

class ProductViewModel : ViewModel() {
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> =
        _allProducts.combine(_searchText) { products, text ->
            if (text.isBlank()) {
                products
            } else {
                products.filter {
                    it.name.contains(text, ignoreCase = true) ||
                            it.category.contains(text, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadProducts()
    }

    private fun loadProducts() {
        _allProducts.value = listOf(
            Product("FR001", "Manzanas Fuji", "Crujientes y dulces, del Valle del Maule.", 1200.0, "https://images.unsplash.com/photo-1560806887-1e4cd0b69665?w=500", 150, "Frutas Frescas"),
            Product("FR002", "Naranjas Valencia", "Jugosas y ricas en vitamina C.", 1000.0, "https://images.unsplash.com/photo-1580053441221-6c4b5c040D53?w=500", 200, "Frutas Frescas"),
            Product("FR003", "Plátanos Cavendish", "Perfectos para el desayuno o snack.", 800.0, "https://images.unsplash.com/photo-1528825871115-3581a5387919?w=500", 250, "Frutas Frescas"),
            Product("VR001", "Zanahorias Orgánicas", "Cultivadas sin pesticidas en O'Higgins.", 900.0, "https://images.unsplash.com/photo-1590868309235-e52bf0f5e262?w=500", 100, "Verduras Orgánicas"),
            Product("VR002", "Espinacas Frescas", "Bolsa de 500g, perfectas para ensaladas.", 700.0, "https://images.unsplash.com/photo-1576045057193-8da4c5f46405?w=500", 80, "Verduras Orgánicas"),
            Product("PO001", "Miel Orgánica", "Frasco de 500g, pura y local.", 5000.0, "https://images.unsplash.com/photo-1558660487-01538357d34e?w=500", 50, "Productos Orgánicos")
        )
    }
    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }
}

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val total: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: Product) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.product.id == product.id }
            if (existingItem != null) {
                currentItems.map {
                    if (it.product.id == product.id) {
                        it.copy(quantity = it.quantity + 1)
                    } else {
                        it
                    }
                }
            } else {
                currentItems + CartItem(product, 1)
            }
        }
        Log.d("CartViewModel", "Items en carro: ${_cartItems.value.size}")
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.update { currentItems ->
            currentItems.filterNot { it.product.id == item.product.id }
        }
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(item)
        } else {
            _cartItems.update { currentItems ->
                currentItems.map {
                    if (it.product.id == item.product.id) {
                        it.copy(quantity = newQuantity)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun checkout() {
        val sale = Sale(
            userId = "mockUserID-123",
            items = _cartItems.value,
            total = total.value,
            timestamp = System.currentTimeMillis()
        )

        Log.d("CartViewModel", "Enviando venta al backend: $sale")
        _cartItems.value = emptyList()
    }
}
