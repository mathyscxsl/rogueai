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

// État de l'UI pour la salle d'attente
data class LobbyUiState(
    val roomCode: String = "",          // Code de la salle
    val players: List<Player> = emptyList(), // Liste des joueurs connectés
    val currentPlayer: Player? = null,  // Le joueur courant
    val isHost: Boolean = false,        // Indique si le joueur courant est l'hôte
    val canStart: Boolean = false,      // Indique si la partie peut démarrer
    val gameState: GameState? = null,   // État actuel du jeu
    val isConnected: Boolean = false,   // Connexion au socket
    val error: String? = null           // Message d'erreur éventuel
)

// ViewModel de la salle d'attente
class LobbyViewModel(
    private val roomCode: String,
    private val socket: RoomSocket       // Socket pour la communication temps réel
) : ViewModel() {

    // StateFlow interne pour gérer l'état de l'UI
    private val _uiState = MutableStateFlow(LobbyUiState(roomCode = roomCode))
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    init {
        observeSocket() // Démarrage de l'observation des flux du socket
    }

    // Observes les différents flux provenant du socket
    private fun observeSocket() {
        // Observation de l'état de connexion
        viewModelScope.launch {
            socket.connected.collect { isConnected ->
                println("LobbyViewModel: Connected changed - $isConnected")
                _uiState.value = _uiState.value.copy(isConnected = isConnected)
            }
        }

        // Observation des informations de la salle (joueurs, hôte, etc.)
        viewModelScope.launch {
            socket.roomInfo.collect { roomInfo ->
                println("LobbyViewModel: RoomInfo received - $roomInfo")
                roomInfo?.let {
                    // Détermine si le joueur courant est l'hôte
                    val isHost = it.players.firstOrNull()?.id == it.you.id
                    // Vérifie si la partie peut démarrer (≥2 joueurs et tous prêts)
                    val canStart = it.players.size >= 2 && it.players.all { player -> player.ready }

                    println("LobbyViewModel: Players count = ${it.players.size}, isHost = $isHost")

                    // Mise à jour de l'état de l'UI
                    _uiState.value = _uiState.value.copy(
                        players = it.players,
                        currentPlayer = it.you,
                        isHost = isHost,
                        canStart = canStart
                    )
                }
            }
        }

        // Observation de l'état du jeu (timer, début de partie, etc.)
        viewModelScope.launch {
            socket.gameState.collect { gameState ->
                println("LobbyViewModel: GameState received - $gameState")
                _uiState.value = _uiState.value.copy(gameState = gameState)
            }
        }
    }

    // Permet au joueur de basculer son état "ready"
    fun toggleReady() {
        val newReadyState = _uiState.value.currentPlayer?.ready?.not() ?: true
        socket.sendReady(newReadyState) // Envoie l'état au serveur via le socket
    }

    // Rafraîchit le nom du joueur sur le serveur
    fun refreshName() {
        socket.refreshName()
    }

    // Nettoyage du ViewModel : fermeture de la connexion socket
    override fun onCleared() {
        super.onCleared()
        socket.closeRoomConnection()
    }
}