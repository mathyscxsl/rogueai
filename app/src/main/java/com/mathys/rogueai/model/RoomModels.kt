package com.mathys.rogueai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRoomRequest(
    val gameType: String = "toggle",
    val soloGame: Boolean = false
)

@JsonClass(generateAdapter = true)
data class RoomRestriction(
    val minPlayer: Int,
    val maxPlayer: Int,
    val gameDuration: Long,
    val roomRestriction: String
)

@JsonClass(generateAdapter = true)
data class CreateRoomResponse(
    val roomCode: String,
    val roomInfo: RoomRestriction
)

@JsonClass(generateAdapter = true)
data class RoomExistsResponse(
    val exists: Boolean
)