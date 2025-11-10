package com.example.huertohogar.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.huertohogar.ui.theme.screen.CartScreen
import com.example.huertohogar.ui.theme.screen.LoginScreen
import com.example.huertohogar.ui.theme.screen.ProductListScreen
import com.example.huertohogar.viewmodels.AuthViewModel
import com.example.huertohogar.viewmodels.CartViewModel
import com.example.huertohogar.viewmodels.ProductViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Inicio de Sesión")
    object ProductList : Screen("product_list", "Productos", Icons.Default.Home)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
    object Profile : Screen("profile", "Mi Perfil", Icons.Default.AccountCircle)

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                ProductList.route -> ProductList
                Cart.route -> Cart
                Profile.route -> Profile
                Login.route -> Login
                else -> ProductList
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val startDestination = Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.ProductList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ProductList.route) {
            ProductListScreen(
                productViewModel = productViewModel,
                cartViewModel = cartViewModel
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(cartViewModel = cartViewModel)
        }
    }
}
