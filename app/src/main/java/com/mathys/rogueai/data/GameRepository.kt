package com.mathys.rogueai.data

import com.mathys.rogueai.network.RoomsApi

/**
 * Repository chargé de faire le lien entre la couche réseau (API)
 * et le reste de l'application.
 *
 * Il centralise les appels liés aux salles de jeu (rooms) afin
 * d'isoler la logique réseau et faciliter les tests / évolutions.
 */
class GameRepository {

    // Instance de l'API responsable des requêtes réseau liées aux rooms
    private val api = RoomsApi()

    /**
     * Crée une nouvelle salle de jeu.
     *
     * @param soloGame indique si la salle est destinée à une partie solo
     * @return la réponse de l'API contenant les informations de la room créée
     */
    suspend fun createRoom(soloGame: Boolean = false) =
        api.createRoom(soloGame)

    /**
     * Vérifie si une salle existe à partir de son code.
     *
     * @param code code unique de la salle
     * @return true si la salle existe, false sinon
     */
    suspend fun checkRoomExists(code: String) =
        api.checkRoomExists(code)

    /**
     * Vérifie l'état de santé du serveur (endpoint de monitoring).
     *
     * @return une réponse indiquant si le backend est accessible
     */
    suspend fun checkHealth() =
        api.checkHealth()
}
