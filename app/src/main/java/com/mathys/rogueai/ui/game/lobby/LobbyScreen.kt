package com.mathys.rogueai.ui.game.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathys.rogueai.model.GameState
import com.mathys.rogueai.ui.common.SfxManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    sfxManager: SfxManager,
    onNavigateToGame: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val previousPlayerCount = remember { mutableStateOf(0) }

    LaunchedEffect(uiState.players.size) {
        if (uiState.players.size > previousPlayerCount.value && previousPlayerCount.value > 0) {
            sfxManager.playSound(SfxManager.PLAYER_JOINED)
        }
        previousPlayerCount.value = uiState.players.size
    }

    LaunchedEffect(uiState.players) {
        val readyPlayers = uiState.players.count { it.ready }
        if (readyPlayers > 0) {
            sfxManager.playSound(SfxManager.PLAYER_READY)
        }
    }

    LaunchedEffect(uiState.gameState) {
        when (uiState.gameState) {
            is GameState.TimerBeforeStart -> {
                sfxManager.playSound(SfxManager.GAME_START)
            }
            is GameState.GameStart -> onNavigateToGame()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partie ${uiState.roomCode}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Joueurs requis : 2-6",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${uiState.players.size} joueur(s) connecté(s)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Joueurs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.players) { player ->
                                PlayerItem(
                                    playerName = player.name,
                                    isReady = player.ready,
                                    isCurrentPlayer = player.id == uiState.currentPlayer?.id,
                                    isHost = uiState.players.firstOrNull()?.id == player.id
                                )
                            }
                        }
                    }
                }

                uiState.error?.let { errorMsg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Erreur",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorMsg,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                when (val state = uiState.gameState) {
                    is GameState.TimerBeforeStart -> {
                        var remainingTime by remember { mutableStateOf(state.duration) }

                        LaunchedEffect(state.duration) {
                            remainingTime = state.duration
                            while (remainingTime > 0) {
                                kotlinx.coroutines.delay(100)
                                remainingTime -= 100
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "La partie commence dans ${(remainingTime / 1000).coerceAtLeast(0)}s",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleReady() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (uiState.currentPlayer?.ready == true) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(
                        if (uiState.currentPlayer?.ready == true) "ANNULER" else "PRÊT",
                        fontSize = 16.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.refreshName() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CHANGER DE NOM")
                }
            }
        }
    }
}

@Composable
fun PlayerItem(
    playerName: String,
    isReady: Boolean,
    isCurrentPlayer: Boolean,
    isHost: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCurrentPlayer)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = playerName,
                fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal
            )
            if (isHost) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "HÔTE",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Surface(
            color = if (isReady)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = if (isReady) "✓ PRÊT" else "EN ATTENTE",
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = if (isReady)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}