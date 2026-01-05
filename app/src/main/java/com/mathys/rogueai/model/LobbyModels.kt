package com.mathys.rogueai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Représente un joueur dans une salle de jeu.
 */
@JsonClass(generateAdapter = true)
data class Player(
    val id: String,        // Identifiant unique du joueur
    val name: String,      // Nom affiché du joueur
    val ready: Boolean     // Indique si le joueur est prêt à lancer la partie
)

/**
 * Informations complètes d’une salle de jeu.
 * Utilisé principalement dans le lobby.
 */
@JsonClass(generateAdapter = true)
data class RoomInfo(
    val you: Player,                       // Représentation du joueur courant
    val players: List<Player>,             // Liste de tous les joueurs présents
    @Json(name = "room_state")
    val roomState: String,                 // État actuel de la salle (waiting, ready, in_game, etc.)
    val level: Int? = null                 // Niveau de difficulté (optionnel selon l’état de la room)
)
