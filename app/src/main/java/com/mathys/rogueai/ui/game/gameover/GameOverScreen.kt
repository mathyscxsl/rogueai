package com.mathys.rogueai.ui.game.gameover
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathys.rogueai.ui.game.components.GameViewModel
import com.mathys.rogueai.ui.common.SfxManager

@Composable
fun GameOverScreen(
    viewModel: GameViewModel,
    sfxManager: SfxManager,
    onNavigateToHome: () -> Unit
) {
    val uiState = viewModel.uiState.value

    LaunchedEffect(uiState.hasWon) {
        if (uiState.hasWon) {
            sfxManager.playSound(SfxManager.VICTORY)
        } else {
            sfxManager.playSound(SfxManager.DEFEAT)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.hasWon)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = if (uiState.hasWon) "VICTOIRE !" else "DÉFAITE",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.hasWon)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )

                Text(
                    text = if (uiState.hasWon)
                        "Vous avez empêché l'IA de dominer le monde !"
                    else
                        "L'IA a pris le contrôle...",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = if (uiState.hasWon)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )

                Divider()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Statistiques",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.hasWon)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )

                    val totalAttempts = uiState.tryHistory.size
                    val successfulAttempts = uiState.tryHistory.count { it.success }
                    val successRate = if (totalAttempts > 0)
                        (successfulAttempts * 100 / totalAttempts)
                    else 0

                    Text(
                        text = "Tentatives totales : $totalAttempts",
                        color = if (uiState.hasWon)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Réussites : $successfulAttempts",
                        color = if (uiState.hasWon)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Taux de réussite : $successRate%",
                        color = if (uiState.hasWon)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Button(
                    onClick = onNavigateToHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RETOUR À L'ACCUEIL")
                }
            }
        }
    }
}