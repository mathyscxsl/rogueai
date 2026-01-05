package com.mathys.rogueai.ui.game.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Écran d'accueil principal du jeu
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,                   // ViewModel de l'écran d'accueil
    onNavigateToLobby: (String) -> Unit         // Callback pour naviguer vers le lobby
) {
    val uiState by viewModel.uiState.collectAsState()            // État réactif du ViewModel
    var showJoinDialog by remember { mutableStateOf(false) }    // Contrôle de l'affichage du dialogue de rejoindre

    // Navigation vers le lobby si demandée
    LaunchedEffect(uiState.navigateToLobby) {
        if (uiState.navigateToLobby && uiState.roomCode != null) {
            onNavigateToLobby(uiState.roomCode!!)
            viewModel.resetNavigation()
        }
    }

    // Animation de pulsation pour le titre
    val infiniteTransition = rememberInfiniteTransition(label = "title")
    val titleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titlePulse"
    )

    // Conteneur principal avec fond dégradé
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0E27), Color(0xFF1A1F3A), Color(0xFF0A0E27))
                )
            )
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // Section titre avec animation et dégradés
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(24.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6200EE).copy(alpha = 0.2f),
                                    Color(0xFF03DAC6).copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF6200EE).copy(alpha = titleAlpha),
                                    Color(0xFF03DAC6).copy(alpha = titleAlpha)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "◢ ROGUE AI ◣",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF03DAC6),
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "OVERRIDE",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 8.sp
                        )
                        // Ligne décorative
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(3.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF6200EE),
                                            Color(0xFF03DAC6),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }

                // Sous-titre avec description du jeu
                Text(
                    text = "Neutralisez l'IA avant qu'elle\nne domine le monde",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }

            // Section des options et boutons
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Carte pour activer/désactiver le mode solo
                SoloModeCard(
                    isSoloMode = uiState.isSoloMode,
                    onToggle = { viewModel.toggleSoloMode() },
                    isEnabled = !uiState.isLoading
                )

                // Avertissement si le mode solo est activé
                if (uiState.isSoloMode) {
                    WarningCard()
                }

                // Bouton pour créer une mission
                CyberpunkButton(
                    text = "▸ CRÉER UNE MISSION",
                    onClick = { viewModel.createRoom() },
                    enabled = !uiState.isLoading,
                    isLoading = uiState.isLoading,
                    isPrimary = true
                )

                // Bouton pour rejoindre une mission
                CyberpunkButton(
                    text = "◉ REJOINDRE UNE MISSION",
                    onClick = { showJoinDialog = true },
                    enabled = !uiState.isLoading,
                    isLoading = false,
                    isPrimary = false
                )
            }

            // Affichage des erreurs éventuelles
            uiState.error?.let { error ->
                ErrorCard(error)
            }
        }
    }

    // Dialogue pour entrer un code de mission
    if (showJoinDialog) {
        CyberpunkJoinDialog(
            onDismiss = { showJoinDialog = false },
            onJoin = { code ->
                viewModel.joinRoom(code)
                showJoinDialog = false
            }
        )
    }
}

// Carte pour activer/désactiver le mode solo
@Composable
fun SoloModeCard(
    isSoloMode: Boolean,
    onToggle: () -> Unit,
    isEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1E1E2E), Color(0xFF2A2A3E))
                )
            )
            .border(
                width = 2.dp,
                color = if (isSoloMode) Color(0xFFFFEB3B).copy(alpha = 0.5f)
                else Color(0xFF6200EE).copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "◦",
                        fontSize = 20.sp,
                        color = if (isSoloMode) Color(0xFFFFEB3B) else Color(0xFF03DAC6)
                    )
                    Text(
                        text = "MODE SOLO",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Entraînement sans autres opérateurs",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Switch pour activer/désactiver le mode solo
            Switch(
                checked = isSoloMode,
                onCheckedChange = { onToggle() },
                enabled = isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFEB3B),
                    checkedTrackColor = Color(0xFFFFEB3B).copy(alpha = 0.3f),
                    uncheckedThumbColor = Color(0xFF03DAC6),
                    uncheckedTrackColor = Color(0xFF03DAC6).copy(alpha = 0.3f)
                )
            )
        }
    }
}

// Carte d'avertissement pour le mode solo
@Composable
fun WarningCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warningPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFEB3B).copy(alpha = warningAlpha * 0.1f))
            .border(
                width = 2.dp,
                color = Color(0xFFFFEB3B).copy(alpha = warningAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠",
                fontSize = 24.sp,
                color = Color(0xFFFFEB3B)
            )
            Text(
                text = "Aucun autre opérateur ne pourra rejoindre cette mission",
                fontSize = 13.sp,
                color = Color.White,
                lineHeight = 18.sp
            )
        }
    }
}

// Bouton stylisé pour l'UI cyberpunk
@Composable
fun CyberpunkButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    isPrimary: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(if (isPrimary) 12.dp else 8.dp, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color(0xFF00FF41) else Color.Transparent,
            disabledContainerColor = Color(0xFF1A1A2E)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (!isPrimary) {
            ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF6200EE), Color(0xFF03DAC6))
                )
            )
        } else null
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = Color(0xFF0A0E27),
                strokeWidth = 3.dp
            )
        } else {
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isPrimary) Color(0xFF0A0E27) else Color(0xFF03DAC6),
                letterSpacing = 2.sp
            )
        }
    }
}

// Carte d'erreur stylisée
@Composable
fun ErrorCard(error: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "error")
    val errorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "errorPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFF1744).copy(alpha = errorAlpha * 0.2f))
            .border(
                width = 2.dp,
                color = Color(0xFFFF1744).copy(alpha = errorAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✕",
                    fontSize = 20.sp,
                    color = Color(0xFFFF1744),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ERREUR SYSTÈME",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF1744),
                    letterSpacing = 2.sp
                )
            }
            Text(
                text = error,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// Dialogue pour entrer un code de mission
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberpunkJoinDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1E2E))
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6200EE).copy(alpha = 0.5f),
                            Color(0xFF03DAC6).copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "◉ REJOINDRE UNE MISSION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF03DAC6),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Entrez le code de la mission",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("CODE MISSION", fontSize = 12.sp, letterSpacing = 1.sp) },
                    placeholder = { Text("Ex: ABC123", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF03DAC6),
                        unfocusedBorderColor = Color(0xFF6200EE).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFF03DAC6),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color(0xFF03DAC6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = Color.White
                    )
                )

                // Boutons Annuler / Valider
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(colors = listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f)))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("ANNULER", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

                    Button(
                        onClick = { onJoin(code) },
                        enabled = code.isNotBlank(),
                        modifier = Modifier.weight(1f).height(56.dp).shadow(8.dp, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF41), disabledContainerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("VALIDER", fontWeight = FontWeight.Black, color = Color(0xFF0A0E27), letterSpacing = 1.sp) }
                }
            }
        }
    }
}