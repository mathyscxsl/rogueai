package com.mathys.rogueai.ui.game.lobby

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1F3A)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CyberpunkTopBar(
                    roomCode = uiState.roomCode,
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    LobbyStatsCard(playerCount = uiState.players.size)

                    PlayersListCard(
                        players = uiState.players,
                        currentPlayerId = uiState.currentPlayer?.id
                    )

                    uiState.error?.let { errorMsg ->
                        ErrorCard(errorMsg)
                    }

                    when (val state = uiState.gameState) {
                        is GameState.TimerBeforeStart -> {
                            CountdownCard(initialDuration = state.duration)
                        }
                        else -> {}
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberpunkButton(
                        text = if (uiState.currentPlayer?.ready == true) "✕ ANNULER" else "✓ PRÊT",
                        onClick = { viewModel.toggleReady() },
                        isReady = uiState.currentPlayer?.ready == true
                    )

                    OutlinedButton(
                        onClick = { viewModel.refreshName() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF03DAC6)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6200EE), Color(0xFF03DAC6))
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "⟳ CHANGER DE NOM",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CyberpunkTopBar(
    roomCode: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E2E),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Text(
                    "←",
                    fontSize = 28.sp,
                    color = Color(0xFF03DAC6),
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SALLE D'ATTENTE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF03DAC6),
                    letterSpacing = 2.sp
                )
                Text(
                    text = roomCode,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
fun LobbyStatsCard(playerCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E1E2E),
                        Color(0xFF2A2A3E)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = Color(0xFF6200EE).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "OPÉRATEURS CONNECTÉS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF03DAC6),
                letterSpacing = 2.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$playerCount",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        playerCount < 2 -> Color(0xFFFF9800)
                        else -> Color(0xFF00FF41)
                    }
                )
                Text(
                    text = "/ 6",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = if (playerCount < 2) "En attente d'opérateurs..." else "Prêt à lancer",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PlayersListCard(
    players: List<com.mathys.rogueai.model.Player>,
    currentPlayerId: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E2E))
            .border(
                width = 2.dp,
                color = Color(0xFF03DAC6).copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "⬢ ÉQUIPE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF03DAC6),
                letterSpacing = 2.sp
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players) { player ->
                    CyberpunkPlayerItem(
                        player = player,
                        isCurrentPlayer = player.id == currentPlayerId,
                        isHost = players.firstOrNull()?.id == player.id
                    )
                }
            }
        }
    }
}

@Composable
fun CyberpunkPlayerItem(
    player: com.mathys.rogueai.model.Player,
    isCurrentPlayer: Boolean,
    isHost: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCurrentPlayer) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6200EE).copy(alpha = 0.3f),
                            Color(0xFF03DAC6).copy(alpha = 0.2f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2A2A3E),
                            Color(0xFF1E1E2E)
                        )
                    )
                }
            )
            .border(
                width = if (isCurrentPlayer) 2.dp else 1.dp,
                color = if (isCurrentPlayer) Color(0xFF03DAC6) else Color(0xFF6200EE).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCurrentPlayer) "▸" else "◦",
                    fontSize = 16.sp,
                    color = if (isCurrentPlayer) Color(0xFF03DAC6) else Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = player.name,
                    fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White,
                    fontSize = 16.sp
                )
                if (isHost) {
                    Text(
                        text = "HOST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A0E27),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00FF41))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (player.ready) Color(0xFF00FF41) else Color(0xFF1A1A2E)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (player.ready) "✓ PRÊT" else "○ ATTENTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (player.ready) Color(0xFF0A0E27) else Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ErrorCard(errorMsg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFF1744).copy(alpha = 0.2f))
            .border(
                width = 2.dp,
                color = Color(0xFFFF1744),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "⚠ ERREUR SYSTÈME",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF1744),
                letterSpacing = 2.sp
            )
            Text(
                text = errorMsg,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CountdownCard(initialDuration: Long) {
    var remainingTime by remember { mutableStateOf(initialDuration) }

    LaunchedEffect(initialDuration) {
        remainingTime = initialDuration
        while (remainingTime > 0) {
            kotlinx.coroutines.delay(100)
            remainingTime -= 100
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "countdown")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF00FF41).copy(alpha = pulseAlpha * 0.2f))
            .border(
                width = 3.dp,
                color = Color(0xFF00FF41).copy(alpha = pulseAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "◉ LANCEMENT IMMINENT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF41),
                letterSpacing = 2.sp
            )
            Text(
                text = "${(remainingTime / 1000).coerceAtLeast(0)}",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun CyberpunkButton(
    text: String,
    onClick: () -> Unit,
    isReady: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(if (isReady) 12.dp else 8.dp, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isReady) Color(0xFFFF9800) else Color(0xFF00FF41)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0A0E27),
            letterSpacing = 2.sp
        )
    }
}