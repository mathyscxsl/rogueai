package com.mathys.rogueai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Représente une commande présente sur le plateau de jeu.
 * Une commande correspond à un élément interactif que le joueur peut manipuler.
 */
@JsonClass(generateAdapter = true)
data class Command(
    val id: String,
    val name: String,
    val type: String,
    val styleType: String,
    @Json(name = "actual_status")
    val actualStatus: String,
    @Json(name = "action_possible")
    val actionPossible: List<String>
)

/**
 * Plateau de jeu contenant l’ensemble des commandes disponibles.
 */
@JsonClass(generateAdapter = true)
data class Board(
    val commands: List<Command>
)

/**
 * Instruction envoyée au joueur.
 * Elle définit une action à réaliser dans un temps donné.
 */
@JsonClass(generateAdapter = true)
data class Instruction(
    @Json(name = "command_id")
    val commandId: String,
    val timeout: Long,
    val timestampCreation: Long,
    @Json(name = "command_type")
    val commandType: String,
    @Json(name = "instruction_text")
    val instructionText: String,
    @Json(name = "expected_status")
    val expectedStatus: String
)

/**
 * État du plateau pour un joueur donné.
 * Combine le board, l’instruction en cours et le niveau de menace.
 */
@JsonClass(generateAdapter = true)
data class PlayerBoard(
    val board: Board,
    val instruction: Instruction,
    val threat: Int
)

/**
 * Historique d’une tentative du joueur.
 */
@JsonClass(generateAdapter = true)
data class TryHistory(
    val time: Long,
    @Json(name = "player_id")
    val playerId: String,
    val success: Boolean
)

/**
 * État final de la partie.
 */
@JsonClass(generateAdapter = true)
data class EndState(
    val win: Boolean,
    val tryHistory: List<TryHistory>
)

/**
 * États globaux possibles du jeu.
 * Utilisé pour piloter la logique et l’UI.
 */
sealed class GameState {

    // En attente de joueurs dans le lobby
    object LobbyWaiting : GameState()

    // Tous les joueurs sont prêts
    object LobbyReady : GameState()

    // Compte à rebours avant le début de la partie
    data class TimerBeforeStart(val duration: Long) : GameState()

    // Début de la partie
    data class GameStart(
        val startThreat: Int,
        val gameDuration: Long
    ) : GameState()

    // Fin de partie
    data class EndState(
        val win: Boolean,
        val tryHistory: List<TryHistory>
    ) : GameState()
}

/**
 * Mise à jour globale du jeu envoyée au client.
 * Peut contenir une mise à jour partielle.
 */
data class GameUpdate(
    val playerBoard: PlayerBoard? = null,
    val gameState: GameState? = null,
    val elapsedTime: Long = 0
)