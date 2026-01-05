package com.mathys.rogueai.ui.game.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathys.rogueai.model.*
import com.mathys.rogueai.network.RoomSocket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// État de l'UI du jeu
data class GameUiState(
    val board: Board? = null,
    val instruction: Instruction? = null,
    val threat: Int = 25,
    val elapsedTime: Long = 0,
    val gameDuration: Long = 180000,
    val remainingInstructionTime: Long = 0,
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    val tryHistory: List<TryHistory> = emptyList()
)

// ViewModel principal du jeu
class GameViewModel(
    private val roomCode: String,
    private val socket: RoomSocket
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameStartTime = 0L
    private var instructionStartTime = 0L

    init {
        observeSocket()
        startTimers()
    }

    // Observation des flux du socket pour recevoir l'état du jeu et du plateau
    private fun observeSocket() {
        viewModelScope.launch {
            socket.gameState.collect { gameState ->
                when (gameState) {
                    is GameState.GameStart -> {
                        gameStartTime = System.currentTimeMillis()
                        _uiState.value = _uiState.value.copy(
                            threat = gameState.startThreat,
                            gameDuration = gameState.gameDuration
                        )
                    }
                    is GameState.EndState -> {
                        _uiState.value = _uiState.value.copy(
                            isGameOver = true,
                            hasWon = gameState.win,
                            tryHistory = gameState.tryHistory
                        )
                    }
                    else -> {} // Ignorer les autres états
                }
            }
        }

        viewModelScope.launch {
            socket.playerBoard.collect { playerBoard ->
                playerBoard?.let {
                    instructionStartTime = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        board = it.board,
                        instruction = it.instruction,
                        threat = it.threat
                    )
                }
            }
        }
    }

    // Timer pour mettre à jour le temps écoulé et le temps restant des instructions
    private fun startTimers() {
        viewModelScope.launch {
            while (!_uiState.value.isGameOver) {
                delay(100)

                if (gameStartTime > 0) {
                    val elapsed = System.currentTimeMillis() - gameStartTime
                    _uiState.value = _uiState.value.copy(elapsedTime = elapsed)
                }

                _uiState.value.instruction?.let { instruction ->
                    val instructionElapsed = System.currentTimeMillis() - instructionStartTime
                    val remaining = (instruction.timeout - instructionElapsed).coerceAtLeast(0)
                    _uiState.value = _uiState.value.copy(remainingInstructionTime = remaining)
                }
            }
        }
    }

    // Envoie l'action d'un joueur au serveur
    fun executeAction(commandId: String, action: String) {
        socket.sendExecuteAction(commandId, action)
    }

    // Nettoyage à la destruction du ViewModel
    override fun onCleared() {
        super.onCleared()
        socket.closeRoomConnection()
    }
}