package com.mathys.rogueai.ui.game.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathys.rogueai.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val roomCode: String? = null,
    val error: String? = null,
    val navigateToLobby: Boolean = false,
    val isSoloMode: Boolean = false
)

class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun createRoom() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.createRoom(soloGame = _uiState.value.isSoloMode).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        roomCode = response.roomCode,
                        navigateToLobby = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erreur lors de la création de la partie"
                    )
                }
            )
        }
    }

    fun joinRoom(code: String) {
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Veuillez entrer un code")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.checkRoomExists(code).fold(
                onSuccess = { exists ->
                    if (exists) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            roomCode = code,
                            navigateToLobby = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Cette partie n'existe pas"
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erreur lors de la vérification"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(navigateToLobby = false)
    }

    fun toggleSoloMode() {
        _uiState.value = _uiState.value.copy(isSoloMode = !_uiState.value.isSoloMode)
    }
}