package com.mathys.rogueai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Command(
    val id: String,
    val name: String,
    val type: String,
    val styleType: String,
    @Json(name = "actual_status") val actualStatus: String,
    @Json(name = "action_possible") val actionPossible: List<String>
)

@JsonClass(generateAdapter = true)
data class Board(
    val commands: List<Command>
)

@JsonClass(generateAdapter = true)
data class Instruction(
    @Json(name = "command_id") val commandId: String,
    val timeout: Long,
    val timestampCreation: Long,
    @Json(name = "command_type") val commandType: String,
    @Json(name = "instruction_text") val instructionText: String,
    @Json(name = "expected_status") val expectedStatus: String
)

@JsonClass(generateAdapter = true)
data class PlayerBoard(
    val board: Board,
    val instruction: Instruction,
    val threat: Int
)

@JsonClass(generateAdapter = true)
data class TryHistory(
    val time: Long,
    @Json(name = "player_id") val playerId: String,
    val success: Boolean
)

@JsonClass(generateAdapter = true)
data class EndState(
    val win: Boolean,
    val tryHistory: List<TryHistory>
)

sealed class GameState {
    object LobbyWaiting : GameState()
    object LobbyReady : GameState()
    data class TimerBeforeStart(val duration: Long) : GameState()
    data class GameStart(val startThreat: Int, val gameDuration: Long) : GameState()
    data class EndState(val win: Boolean, val tryHistory: List<TryHistory>) : GameState()
}

data class GameUpdate(
    val playerBoard: PlayerBoard? = null,
    val gameState: GameState? = null,
    val elapsedTime: Long = 0
)