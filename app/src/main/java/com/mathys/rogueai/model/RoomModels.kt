package com.mathys.rogueai.model

import com.squareup.moshi.JsonClass

/**
 * Requête envoyée au backend pour créer une nouvelle salle de jeu.
 */
@JsonClass(generateAdapter = true)
data class CreateRoomRequest(
    val gameType: String = "toggle",
    val soloGame: Boolean = false
)

/**
 * Contraintes et paramètres de la salle de jeu.
 * Définies par le backend.
 */
@JsonClass(generateAdapter = true)
data class RoomRestriction(
    val minPlayer: Int,
    val maxPlayer: Int,
    val gameDuration: Long,
    val roomRestriction: String
)

/**
 * Réponse du backend après la création d’une salle.
 */
@JsonClass(generateAdapter = true)
data class CreateRoomResponse(
    val roomCode: String,
    val roomInfo: RoomRestriction
)

/**
 * Réponse indiquant si une salle existe ou non.
 */
@JsonClass(generateAdapter = true)
data class RoomExistsResponse(
    val exists: Boolean
)