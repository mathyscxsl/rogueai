package com.mathys.rogueai.ui.game.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathys.rogueai.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Classe représentant l'état de l'écran d'accueil
data class HomeUiState(
    val isLoading: Boolean = false,      // Indique si une action réseau est en cours
    val roomCode: String? = null,        // Code de la salle créée ou jointe
    val error: String? = null,           // Message d'erreur à afficher
    val navigateToLobby: Boolean = false,// Indique si on doit naviguer vers le lobby
    val isSoloMode: Boolean = false      // Mode solo activé ou non
)

// ViewModel pour l'écran d'accueil
class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    // Flow interne pour gérer l'état de l'UI
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow() // Exposition immuable

    // Crée une nouvelle salle de jeu
    fun createRoom() {
        viewModelScope.launch {
            // Mise à jour de l'état : loading activé, suppression d'erreur précédente
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Appel réseau via le repository
            repository.createRoom(soloGame = _uiState.value.isSoloMode).fold(
                onSuccess = { response ->
                    // Si succès, on stocke le code de la room et déclenche la navigation
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        roomCode = response.roomCode,
                        navigateToLobby = true
                    )
                },
                onFailure = { exception ->
                    // Si échec, on met à jour l'état avec le message d'erreur
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erreur lors de la création de la partie"
                    )
                }
            )
        }
    }

    // Permet de rejoindre une salle existante avec un code
    fun joinRoom(code: String) {
        // Vérification rapide du code
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Veuillez entrer un code")
            return
        }

        viewModelScope.launch {
            // Mise à jour de l'état : loading activé, suppression d'erreur précédente
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Vérification de l'existence de la salle via le repository
            repository.checkRoomExists(code).fold(
                onSuccess = { exists ->
                    if (exists) {
                        // La salle existe → navigation vers le lobby
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            roomCode = code,
                            navigateToLobby = true
                        )
                    } else {
                        // La salle n'existe pas → affichage d'erreur
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cette partie n'existe pas"
                        )
                    }
                },
                onFailure = { exception ->
                    // Erreur réseau ou exception → affichage d'erreur générique
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erreur lors de la vérification"
                    )
                }
            )
        }
    }

    // Supprime le message d'erreur affiché
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Réinitialise le flag de navigation vers le lobby
    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(navigateToLobby = false)
    }

    // Active ou désactive le mode solo
    fun toggleSoloMode() {
        _uiState.value = _uiState.value.copy(isSoloMode = !_uiState.value.isSoloMode)
    }
}