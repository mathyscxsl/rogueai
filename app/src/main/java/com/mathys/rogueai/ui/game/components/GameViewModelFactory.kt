package com.mathys.rogueai.ui.game.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mathys.rogueai.network.RoomSocket

// Factory pour créer des instances de GameViewModel avec des paramètres personnalisés
class GameViewModelFactory(
    private val roomCode: String,
    private val socket: RoomSocket
) : ViewModelProvider.Factory {

    // Méthode appelée pour créer le ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Vérifie que le ViewModel demandé est bien un GameViewModel
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(roomCode, socket) as T
        }
        // Si ce n'est pas le bon type, on lève une exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}