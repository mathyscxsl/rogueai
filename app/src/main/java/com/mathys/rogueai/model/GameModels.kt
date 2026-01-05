package com.mathys.rogueai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Représente une commande présente sur le plateau de jeu.
 * Une commande correspond à un élément interactif que le joueur peut manipuler.
 */
@JsonClass(generateAdapter = true)
data class Command(
    val id: String,                 // Identifiant unique de la commande
    val name: String,               // Nom affiché de la commande
    val type: String,               // Type fonctionnel de la commande
    val styleType: String,          // Type de style (UI / visuel)
    @Json(name = "actual_status")
    val actualStatus: String,       // État actuel de la commande
    @Json(name = "action_possible")
    val actionPossible: List<String> // Liste des actions autorisées
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
    val commandId: String,          // Commande concernée par l’instruction
    val timeout: Long,              // Temps maximum pour réaliser l’action
    val timestampCreation: Long,    // Date de création de l’instruction
    @Json(name = "command_type")
    val commandType: String,        // Type de commande attendu
    @Json(name = "instruction_text")
    val instructionText: String,    // Texte affiché au joueur
    @Json(name = "expected_status")
    val expectedStatus: String      // État attendu après l’action
)

/**
 * État du plateau pour un joueur donné.
 * Combine le board, l’instruction en cours et le niveau de menace.
 */
@JsonClass(generateAdapter = true)
data class PlayerBoard(
    val board: Board,               // Plateau de jeu actuel
    val instruction: Instruction,   // Instruction active
    val threat: Int                 // Niveau de menace du joueur
)

/**
 * Historique d’une tentative du joueur.
 */
@JsonClass(generateAdapter = true)
data class TryHistory(
    val time: Long,                 // Timestamp de la tentative
    @Json(name = "player_id")
    val playerId: String,           // Identifiant du joueur
    val success: Boolean            // Indique si la tentative a réussi
)

/**
 * État final de la partie.
 */
@JsonClass(generateAdapter = true)
data class EndState(
    val win: Boolean,               // Résultat de la partie
    val tryHistory: List<TryHistory>// Historique des tentatives
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
        val startThreat: Int,        // Menace initiale
        val gameDuration: Long       // Durée totale de la partie
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
    val playerBoard: PlayerBoard? = null, // Mise à jour du plateau joueur
    val gameState: GameState? = null,     // Changement d’état du jeu
    val elapsedTime: Long = 0             // Temps écoulé depuis le début
)