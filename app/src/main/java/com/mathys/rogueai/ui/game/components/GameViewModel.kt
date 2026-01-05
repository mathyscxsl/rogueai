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
    val board: Board? = null,                     // Plateau du joueur
    val instruction: Instruction? = null,         // Instruction actuelle
    val threat: Int = 25,                         // Niveau de menace
    val elapsedTime: Long = 0,                    // Temps écoulé depuis le début du jeu
    val gameDuration: Long = 180000,              // Durée totale du jeu en ms
    val remainingInstructionTime: Long = 0,       // Temps restant pour l'instruction
    val isGameOver: Boolean = false,              // Indique si le jeu est terminé
    val hasWon: Boolean = false,                  // Indique si le joueur a gagné
    val tryHistory: List<TryHistory> = emptyList()// Historique des tentatives
)

// ViewModel principal du jeu
class GameViewModel(
    private val roomCode: String,   // Code de la salle
    private val socket: RoomSocket  // Gestionnaire de communication réseau
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())       // État mutable interne
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow() // Exposition de l'état en lecture seule

    private var gameStartTime = 0L          // Timestamp du début du jeu
    private var instructionStartTime = 0L   // Timestamp du début de l'instruction actuelle

    init {
        observeSocket()  // Commence à observer les flux du socket
        startTimers()    // Lance les timers pour temps de jeu et instructions
    }

    // Observation des flux du socket pour recevoir l'état du jeu et du plateau
    private fun observeSocket() {
        viewModelScope.launch {
            socket.gameState.collect { gameState ->
                when (gameState) {
                    is GameState.GameStart -> {
                        gameStartTime = System.currentTimeMillis() // Marque le début du jeu
                        _uiState.value = _uiState.value.copy(
                            threat = gameState.startThreat,        // Initialise le niveau de menace
                            gameDuration = gameState.gameDuration // Initialise la durée du jeu
                        )
                    }
                    is GameState.EndState -> {
                        _uiState.value = _uiState.value.copy(
                            isGameOver = true,                     // Marque le jeu comme terminé
                            hasWon = gameState.win,                // Indique si le joueur a gagné
                            tryHistory = gameState.tryHistory      // Historique des tentatives
                        )
                    }
                    else -> {} // Ignorer les autres états
                }
            }
        }

        viewModelScope.launch {
            socket.playerBoard.collect { playerBoard ->
                playerBoard?.let {
                    instructionStartTime = System.currentTimeMillis() // Début de la nouvelle instruction
                    _uiState.value = _uiState.value.copy(
                        board = it.board,                // Met à jour le plateau
                        instruction = it.instruction,    // Met à jour l'instruction
                        threat = it.threat               // Met à jour le niveau de menace
                    )
                }
            }
        }
    }

    // Timer pour mettre à jour le temps écoulé et le temps restant des instructions
    private fun startTimers() {
        viewModelScope.launch {
            while (!_uiState.value.isGameOver) { // Boucle tant que le jeu n'est pas terminé
                delay(100) // Mise à jour toutes les 100ms

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
        socket.closeRoomConnection() // Ferme la connexion au serveur
    }
}