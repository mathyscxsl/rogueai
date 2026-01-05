package com.mathys.rogueai.ui.game.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mathys.rogueai.network.RoomSocket

// Factory pour créer des instances de LobbyViewModel avec les paramètres requis
class LobbyViewModelFactory(
    private val roomCode: String,
    private val socket: RoomSocket
) : ViewModelProvider.Factory {

    // Création du ViewModel avec les paramètres personnalisés
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Vérifie si le type demandé correspond à LobbyViewModel
        if (modelClass.isAssignableFrom(LobbyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LobbyViewModel(roomCode, socket) as T
        }
        // Lance une exception si le type demandé n'est pas supporté
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}