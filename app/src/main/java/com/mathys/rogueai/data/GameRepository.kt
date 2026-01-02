package com.mathys.rogueai.data

import com.mathys.rogueai.network.RoomsApi

class GameRepository {
    private val api = RoomsApi()

    suspend fun createRoom(soloGame: Boolean = false) = api.createRoom(soloGame)

    suspend fun checkRoomExists(code: String) = api.checkRoomExists(code)

    suspend fun checkHealth() = api.checkHealth()
}