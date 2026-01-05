package com.mathys.rogueai.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.mathys.rogueai.model.Command
import com.mathys.rogueai.model.Instruction
import com.mathys.rogueai.ui.common.SfxManager

// Composable principal de l'écran de jeu
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    sfxManager: SfxManager,
    onNavigateToGameOver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val previousThreat = remember { mutableStateOf(uiState.threat) }

    // Effet déclenché quand la menace change
    LaunchedEffect(uiState.threat) {
        if (uiState.threat < previousThreat.value) {
            sfxManager.playSound(SfxManager.CORRECT_ACTION) // Son pour action correcte
        } else if (uiState.threat > previousThreat.value) {
            sfxManager.playSound(SfxManager.WRONG_ACTION)   // Son pour action incorrecte
        }
        previousThreat.value = uiState.threat
    }

    // Effet déclenché si le jeu est terminé
    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            onNavigateToGameOver()
        }
    }

    // Conteneur principal avec un fond dégradé vertical
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header affichant la menace et le temps
            CyberpunkHeader(
                threat = uiState.threat,
                elapsedTime = uiState.elapsedTime,
                gameDuration = uiState.gameDuration
            )

            // Carte d'instruction si disponible
            uiState.instruction?.let { instruction ->
                CyberpunkInstructionCard(
                    instruction = instruction,
                    remainingTime = uiState.remainingInstructionTime
                )
            }

            // Plateau de commandes si disponible
            uiState.board?.let { board ->
                ControlBoard(
                    commands = board.commands,
                    onCommandAction = { commandId, action ->
                        viewModel.executeAction(commandId, action)
                    }
                )
            }
        }
    }
}

// Composable affichant le header cyberpunk (menace + temps restant)
@Composable
fun CyberpunkHeader(
    threat: Int,
    elapsedTime: Long,
    gameDuration: Long
) {
    val remainingSeconds = ((gameDuration - elapsedTime) / 1000).coerceAtLeast(0) // Temps restant en secondes
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")          // Animation infinie pour pulsation

    // Animation alpha pour le pulsé de la menace élevée
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Couleur de la menace selon le niveau
    val threatColor = when {
        threat < 30 -> Color(0xFF00FF41)
        threat < 60 -> Color(0xFFFFEB3B)
        threat < 80 -> Color(0xFFFF9800)
        else -> Color(0xFFFF1744)
    }

    Column(
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
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6200EE).copy(alpha = 0.5f),
                        Color(0xFF03DAC6).copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Affichage du niveau de menace
            Column {
                Text(
                    text = "MENACE MONDIALE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF03DAC6),
                    letterSpacing = 2.sp
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$threat",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = threatColor.copy(alpha = if (threat >= 80) pulseAlpha else 1f) // Pulse si menace critique
                    )
                    Text(
                        text = "%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = threatColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Affichage du temps restant
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TEMPS RESTANT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EE),
                    letterSpacing = 2.sp
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format("%02d", remainingSeconds / 60),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = if (remainingSeconds < 30) Color(0xFFFF1744) else Color.White
                    )
                    Text(
                        text = ":",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = String.format("%02d", remainingSeconds % 60),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = if (remainingSeconds < 30) Color(0xFFFF1744) else Color.White
                    )
                }
            }
        }

        // Barre de progression de la menace
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A2E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (threat / 100f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                threatColor.copy(alpha = 0.8f),
                                threatColor
                            )
                        )
                    )
            )
        }
    }
}

// Composable pour afficher une instruction avec barre de temps
@Composable
fun CyberpunkInstructionCard(
    instruction: Instruction,
    remainingTime: Long
) {
    val progress = (remainingTime.toFloat() / instruction.timeout).coerceIn(0f, 1f) // Calcul du pourcentage restant
    val isUrgent = remainingTime < 5000                                     // Urgent si moins de 5 secondes

    val infiniteTransition = rememberInfiniteTransition(label = "urgent")
    val urgentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgent"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isUrgent) 16.dp else 8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isUrgent) Color(0xFFFF1744).copy(alpha = urgentAlpha * 0.2f)
                else Color(0xFF2A2A3E)
            )
            .border(
                width = if (isUrgent) 3.dp else 2.dp,
                color = if (isUrgent) Color(0xFFFF1744).copy(alpha = urgentAlpha)
                else Color(0xFF03DAC6).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Ligne titre + compteur
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◉ MISSION CRITIQUE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUrgent) Color(0xFFFF1744) else Color(0xFF03DAC6),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${remainingTime / 1000}s",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isUrgent) Color(0xFFFF1744) else Color.White
                )
            }

            // Texte de l'instruction
            Text(
                text = instruction.instructionText.uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 28.sp
            )

            // Barre de progression de l'instruction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1A1A2E))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isUrgent) listOf(Color(0xFFFF5722), Color(0xFFFF1744))
                                else listOf(Color(0xFF6200EE), Color(0xFF03DAC6))
                            )
                        )
                )
            }
        }
    }
}

// Plateau de commandes affiché sous forme de grille
@Composable
fun ControlBoard(
    commands: List<Command>,
    onCommandAction: (String, String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(commands) { command ->
            CyberpunkControlCard(
                command = command,
                onAction = { action -> onCommandAction(command.id, action) }
            )
        }
    }
}

// Carte représentant une commande (toggle ou slider)
@Composable
fun CyberpunkControlCard(
    command: Command,
    onAction: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2A2A3E),
                        Color(0xFF1E1E2E)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF6200EE).copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Informations de la commande
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "▸ ${command.name.uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF03DAC6),
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Text(
                    text = command.type.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }

            // Affichage du contrôle selon le type
            when (command.type) {
                "toggle" -> {
                    CyberpunkToggleControl(
                        isActive = command.actualStatus == "active",
                        onToggle = { onAction("toggle") }
                    )
                }
                "slider" -> {
                    CyberpunkSliderControl(
                        currentValue = command.actualStatus,
                        possibleValues = command.actionPossible,
                        onValueChange = { value -> onAction(value) }
                    )
                }
            }
        }
    }
}

// Composant bouton toggle
@Composable
fun CyberpunkToggleControl(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFF00FF41) else Color(0xFF1A1A2E)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isActive) 8.dp else 0.dp
        )
    ) {
        Text(
            text = if (isActive) "● ACTIF" else "○ INACTIF",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = if (isActive) Color(0xFF0A0E27) else Color.White.copy(alpha = 0.5f)
        )
    }
}

// Composant slider pour choisir une valeur parmi plusieurs
@Composable
fun CyberpunkSliderControl(
    currentValue: String,
    possibleValues: List<String>,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "▸ $currentValue",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF03DAC6)
        )

        // Position actuelle du slider
        var sliderPosition by remember {
            mutableStateOf(possibleValues.indexOf(currentValue).toFloat().coerceAtLeast(0f))
        }

        // Slider affiché
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                val index = sliderPosition.toInt().coerceIn(0, possibleValues.size - 1)
                onValueChange(possibleValues[index])
            },
            valueRange = 0f..(possibleValues.size - 1).toFloat(),
            steps = (possibleValues.size - 2).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF03DAC6),
                activeTrackColor = Color(0xFF6200EE),
                inactiveTrackColor = Color(0xFF1A1A2E)
            )
        )
    }
}
