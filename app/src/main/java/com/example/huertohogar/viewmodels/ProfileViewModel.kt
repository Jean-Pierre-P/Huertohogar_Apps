package com.example.huertohogar.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Data class para almacenar la información del perfil de usuario.
 * Asegúrate de que los nombres de las variables (ej. "nombre")
 * coincidan con los campos de tu documento en Firestore.
 */
data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = ""
)

/**
 * Representa el estado de la UI para la pantalla de perfil.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel para gestionar la lógica de la pantalla de perfil de usuario.
 */
class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Carga el perfil del usuario tan pronto como el ViewModel se inicializa
        fetchUserProfile()
    }

    /**
     * Obtiene los datos del perfil del usuario actual desde Firestore.
     */
    fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado.") }
                return@launch
            }

            try {
                // Asumimos que guardas los perfiles en una colección "users"
                // y cada documento tiene como ID el UID del usuario de auth.
                val docRef = db.collection("users").document(currentUser.uid)
                val document = docRef.get().await()

                if (document.exists()) {
                    // Convierte el documento de Firestore a nuestra data class
                    val profile = document.toObject(UserProfile::class.java)
                    _uiState.update { it.copy(isLoading = false, profile = profile) }
                } else {
                    // Opcional: Si el usuario está logueado pero no tiene perfil en BD,
                    // creamos uno básico con su email y UID.
                    val newProfile = UserProfile(userId = currentUser.uid, email = currentUser.email ?: "")
                    db.collection("users").document(currentUser.uid).set(newProfile).await()
                    _uiState.update { it.copy(isLoading = false, profile = newProfile) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar perfil: ${e.message}") }
            }
        }
    }

    /**
     * Actualiza los datos del perfil del usuario en Firestore.
     */
    fun updateUserProfile(nombre: String, direccion: String, telefono: String) {
        val userId = auth.currentUser?.uid ?: return // No se puede actualizar si no hay usuario

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Creamos un mapa solo con los campos que queremos actualizar
            val updates = mapOf(
                "nombre" to nombre,
                "direccion" to direccion,
                "telefono" to telefono
            )

            try {
                db.collection("users").document(userId).update(updates).await()

                // Actualizamos el estado local para que la UI refleje el cambio al instante
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = it.profile?.copy(
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al actualizar el perfil: ${e.message}") }
            }
        }
    }
}