package com.mathys.rogueai.network

import com.mathys.rogueai.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Gère la communication WebSocket avec une salle de jeu.
 * Permet de recevoir en temps réel l’état du lobby, du jeu et du plateau joueur.
 */
class RoomSocket(
    private val client: OkHttpClient = defaultClient(),
    private val baseUrl: String = "wss://backend.rogueai.surpuissant.io"
) {

    // WebSocket actif
    private var webSocket: WebSocket? = null

    // Scope utilisé pour poster les événements côté UI
    private var scope: CoroutineScope? = null

    /* ---------- StateFlow exposés à l’UI ---------- */

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _roomInfo = MutableStateFlow<RoomInfo?>(null)
    val roomInfo: StateFlow<RoomInfo?> = _roomInfo.asStateFlow()

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _playerBoard = MutableStateFlow<PlayerBoard?>(null)
    val playerBoard: StateFlow<PlayerBoard?> = _playerBoard.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Ouvre une connexion WebSocket vers une salle spécifique.
     *
     * @param roomCode code unique de la salle
     * @param coroutineScope scope de la couche appelante (ViewModel en général)
     */
    fun openRoomConnection(roomCode: String, coroutineScope: CoroutineScope) {
        scope = coroutineScope
        closeRoomConnection()

        println("RoomSocket: Opening connection to room $roomCode")

        val request = Request.Builder()
            .url("$baseUrl/?room=$roomCode")
            .build()

        val listener = RoomWebSocketListener(
            scope = coroutineScope,
            onRoomInfo = { _roomInfo.value = it },
            onGameState = { _gameState.value = it },
            onPlayerBoard = { _playerBoard.value = it },
            onError = { _error.value = it },
            onOpenChanged = { _connected.value = it }
        )

        webSocket = client.newWebSocket(request, listener)
    }

    /**
     * Ferme proprement la connexion WebSocket.
     */
    fun closeRoomConnection(code: Int = 1000, reason: String = "client closing") {
        webSocket?.close(code, reason)
        webSocket = null
        _connected.value = false
    }

    /**
     * Envoie l’état "prêt / non prêt" du joueur.
     */
    fun sendReady(ready: Boolean): Boolean {
        val msg = JSONObject()
            .put("type", "room")
            .put("payload", JSONObject().put("ready", ready))
            .toString()
        return webSocket?.send(msg) ?: false
    }

    /**
     * Demande au serveur de rafraîchir le nom du joueur.
     */
    fun refreshName(): Boolean {
        val msg = JSONObject()
            .put("type", "refresh_name")
            .toString()
        return webSocket?.send(msg) ?: false
    }

    /**
     * Envoie une action exécutée par le joueur sur une commande.
     */
    fun sendExecuteAction(commandId: String, action: String): Boolean {
        val payload = JSONObject()
            .put("command_id", commandId)
            .put("action", action)

        val msg = JSONObject()
            .put("type", "execute_action")
            .put("payload", payload)
            .toString()

        return webSocket?.send(msg) ?: false
    }

    /**
     * Listener WebSocket interne responsable du parsing des messages entrants.
     */
    private inner class RoomWebSocketListener(
        private val scope: CoroutineScope,
        private val onRoomInfo: (RoomInfo) -> Unit,
        private val onGameState: (GameState) -> Unit,
        private val onPlayerBoard: (PlayerBoard) -> Unit,
        private val onError: (String) -> Unit,
        private val onOpenChanged: (Boolean) -> Unit
    ) : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            scope.launch { onOpenChanged(true) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            scope.launch {
                try {
                    val root = JSONObject(text)

                    // Gestion des erreurs envoyées par le serveur
                    if (root.has("error")) {
                        onError(root.optString("error"))
                        return@launch
                    }

                    // Dispatch selon le type de message
                    when (root.optString("type")) {
                        "room_info" -> parseRoomInfo(root)
                        "game_state" -> parseGameState(root)
                        "player_board" -> parsePlayerBoard(root)
                    }
                } catch (t: Throwable) {
                    onError(t.message ?: "Failed to parse message")
                }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            scope.launch { onOpenChanged(false) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            scope.launch {
                onOpenChanged(false)
                onError(t.message ?: "WebSocket failure")
            }
        }

        /**
         * Parse les informations du lobby (joueurs, état de la room).
         */
        private fun parseRoomInfo(root: JSONObject) {
            val payload = root.optJSONObject("payload") ?: return
            val youObj = payload.optJSONObject("you") ?: return
            val playersArray = payload.optJSONArray("players") ?: return

            val you = Player(
                id = youObj.optString("id"),
                name = youObj.optString("name"),
                ready = youObj.optBoolean("ready")
            )

            val players = (0 until playersArray.length()).map {
                val p = playersArray.getJSONObject(it)
                Player(
                    id = p.optString("id"),
                    name = p.optString("name"),
                    ready = p.optBoolean("ready")
                )
            }

            onRoomInfo(
                RoomInfo(
                    you = you,
                    players = players,
                    roomState = payload.optString("room_state"),
                    level = payload.optInt("level", 1)
                )
            )
        }

        /**
         * Parse l’état global du jeu.
         */
        private fun parseGameState(root: JSONObject) {
            val payload = root.optJSONObject("payload") ?: return

            val gameState = when (payload.optString("state")) {
                "lobby_waiting" -> GameState.LobbyWaiting
                "lobby_ready" -> GameState.LobbyReady
                "timer_before_start" ->
                    GameState.TimerBeforeStart(payload.optLong("duration", 3000))
                "game_start" ->
                    GameState.GameStart(
                        payload.optInt("start_threat", 25),
                        payload.optLong("game_duration", 180000)
                    )
                "end_state" -> {
                    val history = payload.optJSONArray("tryHistory")?.let { arr ->
                        (0 until arr.length()).map {
                            val item = arr.getJSONObject(it)
                            TryHistory(
                                time = item.optLong("time"),
                                playerId = item.optString("player_id"),
                                success = item.optBoolean("success")
                            )
                        }
                    } ?: emptyList()
                    GameState.EndState(payload.optBoolean("win"), history)
                }
                else -> null
            }

            gameState?.let(onGameState)
        }

        /**
         * Parse le plateau de jeu spécifique au joueur.
         */
        private fun parsePlayerBoard(root: JSONObject) {
            val payload = root.optJSONObject("payload") ?: return
            val boardObj = payload.optJSONObject("board") ?: return
            val commandsArray = boardObj.optJSONArray("commands") ?: return
            val instructionObj = payload.optJSONObject("instruction") ?: return

            val commands = (0 until commandsArray.length()).mapNotNull {
                val c = commandsArray.getJSONObject(it)
                val actions = c.optJSONArray("action_possible") ?: return@mapNotNull null
                Command(
                    id = c.optString("id"),
                    name = c.optString("name"),
                    type = c.optString("type"),
                    styleType = c.optString("styleType"),
                    actualStatus = c.optString("actual_status"),
                    actionPossible = (0 until actions.length()).map { i -> actions.getString(i) }
                )
            }

            onPlayerBoard(
                PlayerBoard(
                    board = Board(commands),
                    instruction = Instruction(
                        commandId = instructionObj.optString("command_id"),
                        timeout = instructionObj.optLong("timeout"),
                        timestampCreation = instructionObj.optLong("timestampCreation"),
                        commandType = instructionObj.optString("command_type"),
                        instructionText = instructionObj.optString("instruction_text"),
                        expectedStatus = instructionObj.optString("expected_status")
                    ),
                    threat = payload.optInt("threat")
                )
            )
        }
    }

    /**
     * Client WebSocket par défaut.
     */
    companion object {
        private fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // Connexion persistante
                .pingInterval(20, TimeUnit.SECONDS)
                .build()
    }

    /**
     * Réinitialise complètement l’état local.
     * Utile lors d’un changement de room ou d’une déconnexion.
     */
    fun resetAll() {
        closeRoomConnection()
        _roomInfo.value = null
        _gameState.value = null
        _playerBoard.value = null
        _error.value = null
        _connected.value = false
    }
}