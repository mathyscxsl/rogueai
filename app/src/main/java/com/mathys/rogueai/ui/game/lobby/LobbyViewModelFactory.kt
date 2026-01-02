package com.mathys.rogueai.ui.game.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mathys.rogueai.network.RoomSocket

class LobbyViewModelFactory(
    private val roomCode: String,
    private val socket: RoomSocket
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LobbyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LobbyViewModel(roomCode, socket) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}