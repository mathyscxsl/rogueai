package com.mathys.rogueai.model

import com.squareup.moshi.JsonClass

/**
 * Requête envoyée au backend pour créer une nouvelle salle de jeu.
 */
@JsonClass(generateAdapter = true)
data class CreateRoomRequest(
    val gameType: String = "toggle", // Type de jeu (ex: toggle, puzzle, etc.)
    val soloGame: Boolean = false    // Indique si la salle est en mode solo
)

/**
 * Contraintes et paramètres de la salle de jeu.
 * Définies par le backend.
 */
@JsonClass(generateAdapter = true)
data class RoomRestriction(
    val minPlayer: Int,        // Nombre minimum de joueurs requis
    val maxPlayer: Int,        // Nombre maximum de joueurs autorisés
    val gameDuration: Long,    // Durée totale de la partie (en ms)
    val roomRestriction: String // Règles ou restrictions spécifiques à la salle
)

/**
 * Réponse du backend après la création d’une salle.
 */
@JsonClass(generateAdapter = true)
data class CreateRoomResponse(
    val roomCode: String,          // Code unique permettant de rejoindre la salle
    val roomInfo: RoomRestriction  // Informations et restrictions de la salle
)

/**
 * Réponse indiquant si une salle existe ou non.
 */
@JsonClass(generateAdapter = true)
data class RoomExistsResponse(
    val exists: Boolean            // true si la salle existe, false sinon
)