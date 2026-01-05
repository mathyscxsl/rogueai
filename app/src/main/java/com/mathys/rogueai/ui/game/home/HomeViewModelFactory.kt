package com.mathys.rogueai.ui.game.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mathys.rogueai.data.GameRepository

// Factory pour créer des instances de HomeViewModel avec le repository injecté
class HomeViewModelFactory(
    private val repository: GameRepository
) : ViewModelProvider.Factory {

    // Méthode appelée par le système pour créer le ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Vérifie si la classe demandée est HomeViewModel
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Crée et retourne une instance de HomeViewModel avec le repository
            return HomeViewModel(repository) as T
        }
        // Si ce n'est pas HomeViewModel, on lance une exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}