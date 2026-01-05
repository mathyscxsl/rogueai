package com.mathys.rogueai.ui.game.gameover

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
import com.mathys.rogueai.ui.game.components.GameViewModel
import com.mathys.rogueai.ui.common.SfxManager

// Écran affiché à la fin de la partie
@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    sfxManager: SfxManager,
    onNavigateToHome: () -> Unit
) {
    val uiState = viewModel.uiState.value

    // Jouer un son selon la victoire ou la défaite
    LaunchedEffect(uiState.hasWon) {
        if (uiState.hasWon) {
            sfxManager.playSound(SfxManager.VICTORY)
        } else {
            sfxManager.playSound(SfxManager.DEFEAT)
        }
    }

    // Animation de pulsation pour le contour de la boîte principale
    val infiniteTransition = rememberInfiniteTransition(label = "result")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Conteneur principal centré avec fond dégradé selon victoire/défaite
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (uiState.hasWon) {
                        listOf(Color(0xFF0A0E27), Color(0xFF1A3A1A))
                    } else {
                        listOf(Color(0xFF0A0E27), Color(0xFF3A1A1A))
                    }
                )
            )
            .padding(24.dp)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Boîte principale avec bordure et ombre pulsante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (uiState.hasWon) Color(0xFF00FF41).copy(alpha = pulseAlpha * 0.1f)
                    else Color(0xFFFF1744).copy(alpha = pulseAlpha * 0.1f)
                )
                .border(
                    width = 3.dp,
                    color = if (uiState.hasWon) Color(0xFF00FF41).copy(alpha = pulseAlpha)
                    else Color(0xFFFF1744).copy(alpha = pulseAlpha),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Section texte principale (titre + sous-titre + description)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (uiState.hasWon) "◉ MISSION RÉUSSIE" else "✕ MISSION ÉCHOUÉE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.hasWon) Color(0xFF00FF41) else Color(0xFFFF1744),
                        letterSpacing = 3.sp
                    )

                    Text(
                        text = if (uiState.hasWon) "VICTOIRE" else "DÉFAITE",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )

                    Text(
                        text = if (uiState.hasWon) {
                            "L'IA a été neutralisée avec succès"
                        } else {
                            "L'IA a pris le contrôle du système"
                        },
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }

                Divider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // Rapport de mission détaillé
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E2E))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF6200EE).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Titre du rapport
                        Text(
                            text = "▸ RAPPORT DE MISSION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF03DAC6),
                            letterSpacing = 2.sp
                        )

                        // Calcul des statistiques
                        val totalAttempts = uiState.tryHistory.size
                        val successfulAttempts = uiState.tryHistory.count { it.success }
                        val successRate = if (totalAttempts > 0) (successfulAttempts * 100 / totalAttempts) else 0

                        // Statistiques détaillées
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatRow(
                                label = "Actions totales",
                                value = totalAttempts.toString(),
                                color = Color.White
                            )
                            StatRow(
                                label = "Actions réussies",
                                value = successfulAttempts.toString(),
                                color = Color(0xFF00FF41)
                            )
                            StatRow(
                                label = "Actions échouées",
                                value = (totalAttempts - successfulAttempts).toString(),
                                color = Color(0xFFFF1744)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Taux de réussite global
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF6200EE).copy(alpha = 0.3f),
                                                Color(0xFF03DAC6).copy(alpha = 0.3f)
                                            )
                                        )
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "TAUX DE RÉUSSITE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF03DAC6),
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = "$successRate%",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when {
                                            successRate >= 80 -> Color(0xFF00FF41)
                                            successRate >= 50 -> Color(0xFFFFEB3B)
                                            else -> Color(0xFFFF1744)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bouton pour retourner à la base
                Button(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(12.dp, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.hasWon) Color(0xFF00FF41) else Color(0xFFFF1744)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "← RETOUR À LA BASE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0A0E27),
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

// Composable pour afficher une ligne de statistiques
@Composable
fun StatRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}