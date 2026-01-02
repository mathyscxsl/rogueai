package com.mathys.rogueai.ui.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathys.rogueai.model.Command
import com.mathys.rogueai.model.Instruction
import com.mathys.rogueai.ui.common.SfxManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    sfxManager: SfxManager,
    onNavigateToGameOver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val previousThreat = remember { mutableStateOf(uiState.threat) }

    LaunchedEffect(uiState.threat) {
        if (uiState.threat < previousThreat.value) {
            sfxManager.playSound(SfxManager.CORRECT_ACTION)
        } else if (uiState.threat > previousThreat.value) {
            sfxManager.playSound(SfxManager.WRONG_ACTION)
        }
        previousThreat.value = uiState.threat
    }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            onNavigateToGameOver()
        }
    }

    Scaffold(
        topBar = {
            GameHeader(
                threat = uiState.threat,
                elapsedTime = uiState.elapsedTime,
                gameDuration = uiState.gameDuration
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.instruction?.let { instruction ->
                InstructionCard(
                    instruction = instruction,
                    remainingTime = uiState.remainingInstructionTime
                )
            }

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

@Composable
fun GameHeader(
    threat: Int,
    elapsedTime: Long,
    gameDuration: Long
) {
    val remainingSeconds = ((gameDuration - elapsedTime) / 1000).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MENACE: $threat%",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${remainingSeconds}s",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        LinearProgressIndicator(
            progress = (threat / 100f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = when {
                threat < 30 -> Color.Green
                threat < 60 -> Color.Yellow
                threat < 80 -> Color(0xFFFF9800)
                else -> Color.Red
            }
        )
    }
}

@Composable
fun InstructionCard(
    instruction: Instruction,
    remainingTime: Long
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INSTRUCTION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "${remainingTime / 1000}s",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingTime < 5000) Color.Red else MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Text(
                text = instruction.instructionText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            LinearProgressIndicator(
                progress = (remainingTime.toFloat() / instruction.timeout).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}

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
            ControlCard(
                command = command,
                onAction = { action -> onCommandAction(command.id, action) }
            )
        }
    }
}

@Composable
fun ControlCard(
    command: Command,
    onAction: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = command.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            when (command.type) {
                "toggle" -> {
                    ToggleControl(
                        isActive = command.actualStatus == "active",
                        onToggle = { onAction("toggle") }
                    )
                }
                "slider" -> {
                    SliderControl(
                        currentValue = command.actualStatus,
                        possibleValues = command.actionPossible,
                        onValueChange = { value -> onAction(value) }
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleControl(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Button(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(if (isActive) "ACTIF" else "INACTIF")
    }
}

@Composable
fun SliderControl(
    currentValue: String,
    possibleValues: List<String>,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Valeur: $currentValue",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        var sliderPosition by remember {
            mutableStateOf(possibleValues.indexOf(currentValue).toFloat().coerceAtLeast(0f))
        }

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                val index = sliderPosition.toInt().coerceIn(0, possibleValues.size - 1)
                onValueChange(possibleValues[index])
            },
            valueRange = 0f..(possibleValues.size - 1).toFloat(),
            steps = (possibleValues.size - 2).coerceAtLeast(0)
        )
    }
}