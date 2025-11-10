package com.example.huertohogar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.huertohogar.navigation.AppNavHost
import com.example.huertohogar.navigation.Screen
import com.example.huertohogar.ui.theme.HuertoHogarTheme
import com.example.huertohogar.viewmodels.AuthViewModel
import com.example.huertohogar.viewmodels.CartViewModel
import com.example.huertohogar.viewmodels.ProductViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HuertoHogarTheme {
                MainScreen(authViewModel, cartViewModel, productViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val cartItems by cartViewModel.cartItems.collectAsStateWithLifecycle()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val cartItemCount = cartItems.sumOf { it.quantity }

    val showMainUI = isLoggedIn && currentRoute != Screen.Login.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showMainUI,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Huerto Hogar",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Productos") },
                    label = { Text("Productos") },
                    selected = currentRoute == Screen.ProductList.route,
                    onClick = {
                        navController.navigate(Screen.ProductList.route)
                        scope.launch { drawerState.close() }
                    }
                )

                // --- INICIO DE LA MODIFICACIÓN ---
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Perfil") },
                    label = { Text("Mi Perfil") },
                    // Ahora se marca como seleccionado si la ruta es la del perfil
                    selected = currentRoute == Screen.Profile.route,
                    onClick = {
                        // Ahora navega a la pantalla de perfil
                        navController.navigate(Screen.Profile.route)
                        scope.launch { drawerState.close() }
                    }
                )
                // --- FIN DE LA MODIFICACIÓN ---

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Mis Pedidos") },
                    label = { Text("Mis Pedidos") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión") },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showMainUI) {
                    HuertoTopAppBar(
                        title = Screen.fromRoute(currentRoute).title,
                        cartItemCount = cartItemCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onCartClick = { navController.navigate(Screen.Cart.route) }
                    )
                }
            },
            bottomBar = {
                if (showMainUI) {
                    HuertoBottomBar(
                        navController = navController,
                        currentRoute = currentRoute!!
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavHost(
                    navController = navController,
                    authViewModel = authViewModel,
                    cartViewModel = cartViewModel,
                    productViewModel = productViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuertoTopAppBar(
    title: String,
    cartItemCount: Int,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
            }
        },
        actions = {
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge { Text("$cartItemCount") }
                    }
                }
            ) {
                IconButton(onClick = onCartClick) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Ver carro de compras")
                }
            }
        }
    )
}

@Composable
fun HuertoBottomBar(navController: NavHostController, currentRoute: String) {
    NavigationBar {
        val items = listOf(Screen.ProductList, Screen.Cart)
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon ?: Icons.Default.Home,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
