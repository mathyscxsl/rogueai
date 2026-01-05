package com.mathys.rogueai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Représente un joueur dans une salle de jeu.
 */
@JsonClass(generateAdapter = true)
data class Player(
    val id: String,
    val name: String,
    val ready: Boolean
)

/**
 * Informations complètes d’une salle de jeu.
 * Utilisé principalement dans le lobby.
 */
@JsonClass(generateAdapter = true)
data class RoomInfo(
    val you: Player,
    val players: List<Player>,
    @Json(name = "room_state")
    val roomState: String,
    val level: Int? = null
)
