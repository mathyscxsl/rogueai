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

sealed class Screen {
    object Home : Screen()
    data class Lobby(val roomCode: String) : Screen()
    data class Game(val roomCode: String) : Screen()
    object GameOver : Screen()
}

class MainActivity : ComponentActivity() {
    private val gameRepository = GameRepository()
    private var roomSocket: RoomSocket? = null
    private var currentGameViewModel: GameViewModel? = null
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var sfxManager: SfxManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sfxManager = SfxManager(this)
        sfxManager.loadSounds()

        setContent {
            RogueAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RogueAIApp()
                }
            }
        }
    }

    @Composable
    fun RogueAIApp() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

        when (val screen = currentScreen) {
            is Screen.Home -> {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(gameRepository)
                )

                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToLobby = { roomCode ->
                        roomSocket = RoomSocket()
                        roomSocket?.openRoomConnection(roomCode, activityScope)
                        sfxManager.startBackgroundMusic()
                        currentScreen = Screen.Lobby(roomCode)
                    }
                )
            }

            is Screen.Lobby -> {
                roomSocket?.let { socket ->
                    val lobbyViewModel: LobbyViewModel = viewModel(
                        key = "lobby_${screen.roomCode}",
                        factory = LobbyViewModelFactory(screen.roomCode, socket)
                    )

                    LobbyScreen(
                        viewModel = lobbyViewModel,
                        sfxManager = sfxManager,
                        onNavigateToGame = {
                            currentScreen = Screen.Game(screen.roomCode)
                        },
                        onNavigateBack = {
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
                    val gameViewModel: GameViewModel = viewModel(
                        key = "game_${screen.roomCode}",
                        factory = GameViewModelFactory(screen.roomCode, socket)
                    )
                    currentGameViewModel = gameViewModel

                    GameScreen(
                        viewModel = gameViewModel,
                        sfxManager = sfxManager,
                        onNavigateToGameOver = {
                            sfxManager.stopBackgroundMusic()
                            currentScreen = Screen.GameOver
                        }
                    )
                }
            }

            is Screen.GameOver -> {
                currentGameViewModel?.let { viewModel ->
                    GameOverScreen(
                        viewModel = viewModel,
                        sfxManager = sfxManager,
                        onNavigateToHome = {
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

    override fun onDestroy() {
        super.onDestroy()
        roomSocket?.closeRoomConnection()
        roomSocket = null
        sfxManager.release()
    }

    override fun onPause() {
        super.onPause()
        sfxManager.pauseBackgroundMusic()
    }

    override fun onResume() {
        super.onResume()
        sfxManager.resumeBackgroundMusic()
    }
}