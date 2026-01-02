package com.mathys.rogueai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Player(
    val id: String,
    val name: String,
    val ready: Boolean
)

@JsonClass(generateAdapter = true)
data class RoomInfo(
    val you: Player,
    val players: List<Player>,
    @Json(name = "room_state") val roomState: String,
    val level: Int? = null
)