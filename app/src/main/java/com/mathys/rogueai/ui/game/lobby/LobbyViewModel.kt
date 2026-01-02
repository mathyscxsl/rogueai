package com.mathys.rogueai.ui.game.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathys.rogueai.model.GameState
import com.mathys.rogueai.model.Player
import com.mathys.rogueai.network.RoomSocket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LobbyUiState(
    val roomCode: String = "",
    val players: List<Player> = emptyList(),
    val currentPlayer: Player? = null,
    val isHost: Boolean = false,
    val canStart: Boolean = false,
    val gameState: GameState? = null,
    val isConnected: Boolean = false,
    val error: String? = null
)

class LobbyViewModel(
    private val roomCode: String,
    private val socket: RoomSocket
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState(roomCode = roomCode))
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    init {
        observeSocket()
    }

    private fun observeSocket() {
        viewModelScope.launch {
            socket.connected.collect { isConnected ->
                println("LobbyViewModel: Connected changed - $isConnected")
                _uiState.value = _uiState.value.copy(isConnected = isConnected)
            }
        }

        viewModelScope.launch {
            socket.roomInfo.collect { roomInfo ->
                println("LobbyViewModel: RoomInfo received - $roomInfo")
                roomInfo?.let {
                    val isHost = it.players.firstOrNull()?.id == it.you.id
                    val canStart = it.players.size >= 2 && it.players.all { player -> player.ready }

                    println("LobbyViewModel: Players count = ${it.players.size}, isHost = $isHost")

                    _uiState.value = _uiState.value.copy(
                        players = it.players,
                        currentPlayer = it.you,
                        isHost = isHost,
                        canStart = canStart
                    )
                }
            }
        }

        viewModelScope.launch {
            socket.gameState.collect { gameState ->
                println("LobbyViewModel: GameState received - $gameState")
                _uiState.value = _uiState.value.copy(gameState = gameState)
            }
        }
    }

    fun toggleReady() {
        val newReadyState = _uiState.value.currentPlayer?.ready?.not() ?: true
        socket.sendReady(newReadyState)
    }

    fun refreshName() {
        socket.refreshName()
    }

    override fun onCleared() {
        super.onCleared()
        socket.closeRoomConnection()
    }
}