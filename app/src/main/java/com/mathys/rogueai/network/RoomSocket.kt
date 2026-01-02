package com.mathys.rogueai.network

import com.mathys.rogueai.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RoomSocket(
    private val client: OkHttpClient = defaultClient(),
    private val baseUrl: String = "wss://backend.rogueai.surpuissant.io"
) {
    private var webSocket: WebSocket? = null
    private var scope: CoroutineScope? = null

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

    fun openRoomConnection(roomCode: String, coroutineScope: CoroutineScope) {
        scope = coroutineScope
        closeRoomConnection()

        println("RoomSocket: Opening connection to room $roomCode")

        val request = Request.Builder()
            .url("$baseUrl/?room=$roomCode")
            .build()

        val listener = RoomWebSocketListener(
            scope = coroutineScope,
            onRoomInfo = { info ->
                println("RoomSocket: Received room info - ${info.players.size} players")
                _roomInfo.value = info
            },
            onGameState = { state ->
                println("RoomSocket: Received game state - $state")
                _gameState.value = state
            },
            onPlayerBoard = { board ->
                println("RoomSocket: Received player board")
                _playerBoard.value = board
            },
            onError = { err ->
                println("RoomSocket: Error - $err")
                _error.value = err
            },
            onOpenChanged = { isOpen ->
                println("RoomSocket: Connection open changed - $isOpen")
                _connected.value = isOpen
            }
        )

        webSocket = client.newWebSocket(request, listener)
    }

    fun closeRoomConnection(code: Int = 1000, reason: String = "client closing") {
        webSocket?.close(code, reason)
        webSocket = null
        _connected.value = false
    }

    fun sendReady(ready: Boolean): Boolean {
        val msg = JSONObject()
            .put("type", "room")
            .put("payload", JSONObject().put("ready", ready))
            .toString()
        return webSocket?.send(msg) ?: false
    }

    fun refreshName(): Boolean {
        val msg = JSONObject()
            .put("type", "refresh_name")
            .toString()
        return webSocket?.send(msg) ?: false
    }

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
                    println("RoomSocket: Received message - $text")
                    val root = JSONObject(text)

                    if (root.has("error")) {
                        val errorMsg = root.optString("error")
                        println("RoomSocket: Server error - $errorMsg")
                        onError(errorMsg)
                        return@launch
                    }

                    when (root.optString("type")) {
                        "room_info" -> parseRoomInfo(root)
                        "game_state" -> parseGameState(root)
                        "player_board" -> parsePlayerBoard(root)
                        else -> println("RoomSocket: Unknown message type - ${root.optString("type")}")
                    }
                } catch (t: Throwable) {
                    println("RoomSocket: Parse error - ${t.message}")
                    t.printStackTrace()
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

        private fun parseRoomInfo(root: JSONObject) {
            println("RoomSocket: Parsing room info")
            val payload = root.optJSONObject("payload") ?: run {
                println("RoomSocket: No payload in room_info")
                return
            }
            val youObj = payload.optJSONObject("you") ?: run {
                println("RoomSocket: No 'you' in payload")
                return
            }
            val playersArray = payload.optJSONArray("players") ?: run {
                println("RoomSocket: No 'players' in payload")
                return
            }

            val you = Player(
                id = youObj.optString("id"),
                name = youObj.optString("name"),
                ready = youObj.optBoolean("ready")
            )

            val players = mutableListOf<Player>()
            for (i in 0 until playersArray.length()) {
                val p = playersArray.getJSONObject(i)
                players.add(
                    Player(
                        id = p.optString("id"),
                        name = p.optString("name"),
                        ready = p.optBoolean("ready")
                    )
                )
            }

            println("RoomSocket: Parsed ${players.size} players, you = ${you.name}")

            onRoomInfo(
                RoomInfo(
                    you = you,
                    players = players,
                    roomState = payload.optString("room_state"),
                    level = payload.optInt("level", 1)
                )
            )
        }

        private fun parseGameState(root: JSONObject) {
            val payload = root.optJSONObject("payload") ?: return
            val state = payload.optString("state")

            val gameState = when (state) {
                "lobby_waiting" -> GameState.LobbyWaiting
                "lobby_ready" -> GameState.LobbyReady
                "timer_before_start" -> {
                    val duration = payload.optLong("duration", 3000)
                    GameState.TimerBeforeStart(duration)
                }
                "game_start" -> {
                    val startThreat = payload.optInt("start_threat", 25)
                    val gameDuration = payload.optLong("game_duration", 180000)
                    GameState.GameStart(startThreat, gameDuration)
                }
                "end_state" -> {
                    val win = payload.optBoolean("win", false)
                    val tryHistory = payload.optJSONArray("tryHistory")?.let { arr ->
                        (0 until arr.length()).map { i ->
                            val item = arr.getJSONObject(i)
                            TryHistory(
                                time = item.optLong("time"),
                                playerId = item.optString("player_id"),
                                success = item.optBoolean("success")
                            )
                        }
                    } ?: emptyList()
                    GameState.EndState(win, tryHistory)
                }
                else -> null
            }

            gameState?.let { onGameState(it) }
        }

        private fun parsePlayerBoard(root: JSONObject) {
            val payload = root.optJSONObject("payload") ?: return
            val boardObj = payload.optJSONObject("board") ?: return
            val commandsArray = boardObj.optJSONArray("commands") ?: return
            val instructionObj = payload.optJSONObject("instruction") ?: return

            val commands = mutableListOf<Command>()
            for (i in 0 until commandsArray.length()) {
                val c = commandsArray.getJSONObject(i)
                val actionsArray = c.optJSONArray("action_possible") ?: continue
                val actions = (0 until actionsArray.length()).map { actionsArray.getString(it) }

                commands.add(
                    Command(
                        id = c.optString("id"),
                        name = c.optString("name"),
                        type = c.optString("type"),
                        styleType = c.optString("styleType"),
                        actualStatus = c.optString("actual_status"),
                        actionPossible = actions
                    )
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

    companion object {
        private fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .pingInterval(20, TimeUnit.SECONDS)
                .build()
        }
    }

    fun resetAll() {
        closeRoomConnection()
        _roomInfo.value = null
        _gameState.value = null
        _playerBoard.value = null
        _error.value = null
        _connected.value = false
    }
}