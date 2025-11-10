package com.example.huertohogar.ui.theme.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.huertohogar.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    var nombre by remember(uiState.profile?.nombre) {
        mutableStateOf(uiState.profile?.nombre ?: "")
    }
    var direccion by remember(uiState.profile?.direccion) {
        mutableStateOf(uiState.profile?.direccion ?: "")
    }
    var telefono by remember(uiState.profile?.telefono) {
        mutableStateOf(uiState.profile?.telefono ?: "")
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.errorMessage != null -> {
                Text("Error: ${uiState.errorMessage}")
                Button(onClick = { profileViewModel.fetchUserProfile() }) {
                    Text("Reintentar")
                }
            }
            uiState.profile != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Mi Perfil",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    OutlinedTextField(
                        value = uiState.profile!!.email, // Es seguro usar !! aquí
                        onValueChange = {},
                        label = { Text("Email") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            profileViewModel.updateUserProfile(nombre, direccion, telefono)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}