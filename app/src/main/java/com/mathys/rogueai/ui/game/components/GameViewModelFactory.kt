package com.mathys.rogueai.ui.game.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mathys.rogueai.network.RoomSocket

class GameViewModelFactory(
    private val roomCode: String,
    private val socket: RoomSocket
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(roomCode, socket) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}