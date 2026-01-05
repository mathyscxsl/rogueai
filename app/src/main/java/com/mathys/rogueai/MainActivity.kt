package com.mathys.rogueai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.mathys.rogueai.data.GameRepository
import com.mathys.rogueai.network.RoomSocket
import com.mathys.rogueai.ui.game.components.*
import com.mathys.rogueai.ui.game.gameover.GameOverScreen
import com.mathys.rogueai.ui.game.home.HomeScreen
import com.mathys.rogueai.ui.game.home.HomeViewModel
import com.mathys.rogueai.ui.game.home.HomeViewModelFactory
import com.mathys.rogueai.ui.game.lobby.LobbyScreen
import com.mathys.rogueai.ui.game.lobby.LobbyViewModel
import com.mathys.rogueai.ui.game.lobby.LobbyViewModelFactory
import com.mathys.rogueai.ui.theme.RogueAITheme
import com.mathys.rogueai.ui.common.SfxManager

// Définition des écrans de l'application
sealed class Screen {
    object Home : Screen()                     // Écran d'accueil
    data class Lobby(val roomCode: String) : Screen() // Salle d'attente avec code
    data class Game(val roomCode: String) : Screen()  // Écran de jeu
    object GameOver : Screen()                 // Écran de fin de partie
}

// Activité principale de l'application
class MainActivity : ComponentActivity() {

    // Repository pour la logique et la communication avec le serveur
    private val gameRepository = GameRepository()

    // Socket pour la communication en temps réel dans une salle
    private var roomSocket: RoomSocket? = null

    // ViewModel du jeu courant, conservé entre les écrans
    private var currentGameViewModel: GameViewModel? = null

    // Scope pour lancer des coroutines liées à l'activité
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Gestionnaire des effets sonores et musique
    private lateinit var sfxManager: SfxManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du gestionnaire de sons et chargement des effets
        sfxManager = SfxManager(this)
        sfxManager.loadSounds()

        // Définition du contenu Compose de l'activité
        setContent {
            RogueAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RogueAIApp() // App composable principale
                }
            }
        }
    }

    // Composable principal qui gère la navigation entre les écrans
    @Composable
    fun RogueAIApp() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

        when (val screen = currentScreen) {

            is Screen.Home -> {
                // Création du ViewModel pour l'écran d'accueil
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(gameRepository)
                )

                // Écran d'accueil avec navigation vers la salle
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToLobby = { roomCode ->
                        // Création et ouverture de la connexion socket
                        roomSocket = RoomSocket()
                        roomSocket?.openRoomConnection(roomCode, activityScope)
                        sfxManager.startBackgroundMusic()
                        currentScreen = Screen.Lobby(roomCode)
                    }
                )
            }

            is Screen.Lobby -> {
                roomSocket?.let { socket ->
                    // Création du ViewModel pour la salle d'attente
                    val lobbyViewModel: LobbyViewModel = viewModel(
                        key = "lobby_${screen.roomCode}",
                        factory = LobbyViewModelFactory(screen.roomCode, socket)
                    )

                    LobbyScreen(
                        viewModel = lobbyViewModel,
                        sfxManager = sfxManager,
                        onNavigateToGame = {
                            // Passage à l'écran de jeu
                            currentScreen = Screen.Game(screen.roomCode)
                        },
                        onNavigateBack = {
                            // Retour à l'écran d'accueil : nettoyage
                            socket.resetAll()
                            roomSocket = null
                            sfxManager.stopBackgroundMusic()
                            currentScreen = Screen.Home
                        }
                    )
                }
            }

            is Screen.Game -> {
                roomSocket?.let { socket ->
                    // Création du ViewModel pour l'écran de jeu
                    val gameViewModel: GameViewModel = viewModel(
                        key = "game_${screen.roomCode}",
                        factory = GameViewModelFactory(screen.roomCode, socket)
                    )
                    currentGameViewModel = gameViewModel

                    GameScreen(
                        viewModel = gameViewModel,
                        sfxManager = sfxManager,
                        onNavigateToGameOver = {
                            // Passage à l'écran de fin de partie
                            sfxManager.stopBackgroundMusic()
                            currentScreen = Screen.GameOver
                        }
                    )
                }
            }

            is Screen.GameOver -> {
                currentGameViewModel?.let { viewModel ->
                    // Écran de fin de partie
                    GameOverScreen(
                        viewModel = viewModel,
                        sfxManager = sfxManager,
                        onNavigateToHome = {
                            // Nettoyage et retour à l'accueil
                            roomSocket?.resetAll()
                            roomSocket = null
                            currentGameViewModel = null
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }
    }

    // Fermeture des ressources lors de la destruction de l'activité
    override fun onDestroy() {
        super.onDestroy()
        roomSocket?.closeRoomConnection()
        roomSocket = null
        sfxManager.release()
    }

    // Pause de la musique de fond lorsque l'application est en arrière-plan
    override fun onPause() {
        super.onPause()
        sfxManager.pauseBackgroundMusic()
    }

    // Reprise de la musique de fond lorsque l'application revient au premier plan
    override fun onResume() {
        super.onResume()
        sfxManager.resumeBackgroundMusic()
    }
}