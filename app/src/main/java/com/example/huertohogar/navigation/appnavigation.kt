package com.example.huertohogar.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.huertohogar.ui.theme.screen.CartScreen
import com.example.huertohogar.ui.theme.screen.LoginScreen
import com.example.huertohogar.ui.theme.screen.ProductListScreen
import com.example.huertohogar.ui.theme.screen.ProfileScreen
import com.example.huertohogar.viewmodels.AuthViewModel
import com.example.huertohogar.viewmodels.CartViewModel
import com.example.huertohogar.viewmodels.ProductViewModel
import com.google.android.engage.social.datamodel.Profile

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Inicio de Sesión")
    object ProductList : Screen("productList", "Productos", Icons.Default.Home)
    object Cart : Screen("cart", "Carro de Compras", Icons.Default.ShoppingCart)

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                Login.route -> Login
                ProductList.route -> ProductList
                Cart.route -> Cart
                null -> ProductList
                else -> ProductList
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Navega a la lista de productos y limpia el stack
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
            CartScreen(
                cartViewModel = cartViewModel
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel = viewModel())
        }
    }
}