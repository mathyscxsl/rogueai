package com.mathys.rogueai.network

import com.mathys.rogueai.model.CreateRoomRequest
import com.mathys.rogueai.model.CreateRoomResponse
import com.mathys.rogueai.model.RoomExistsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API responsable des communications réseau liées aux salles de jeu (rooms).
 * Utilise OkHttp pour les requêtes HTTP et Moshi pour la sérialisation JSON.
 */
class RoomsApi {

    /**
     * Client HTTP configuré avec des timeouts explicites
     * afin d’éviter les blocages réseau prolongés.
     */
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Instance Moshi pour la conversion JSON ↔ objets Kotlin.
     */
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // URL de base du backend RogueAI
    private val baseUrl = "https://backend.rogueai.surpuissant.io"

    /**
     * Crée une nouvelle salle de jeu côté backend.
     *
     * @param soloGame indique si la salle est en mode solo
     * @return Result contenant soit la réponse de création de salle,
     * soit une erreur réseau ou de parsing.
     */
    suspend fun createRoom(
        soloGame: Boolean = false
    ): Result<CreateRoomResponse> = withContext(Dispatchers.IO) {
        try {
            // Construction du corps de la requête
            val requestData = CreateRoomRequest(soloGame = soloGame)
            val json = moshi
                .adapter(CreateRoomRequest::class.java)
                .toJson(requestData)

            // Construction de la requête HTTP POST
            val request = Request.Builder()
                .url("$baseUrl/create-room")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            response.use {
                if (it.isSuccessful) {
                    val body = it.body?.string()
                    if (body != null) {
                        // Désérialisation de la réponse JSON
                        val roomResponse = moshi
                            .adapter(CreateRoomResponse::class.java)
                            .fromJson(body)

                        if (roomResponse != null) {
                            Result.success(roomResponse)
                        } else {
                            Result.failure(IOException("Invalid response format"))
                        }
                    } else {
                        Result.failure(IOException("Empty response body"))
                    }
                } else {
                    // Erreur HTTP explicite avec code + message
                    val errorBody = it.body?.string() ?: "Unknown error"
                    Result.failure(IOException("HTTP ${it.code}: $errorBody"))
                }
            }
        } catch (e: Exception) {
            // Gestion centralisée des erreurs réseau
            Result.failure(IOException("Network error: ${e.message}", e))
        }
    }

    /**
     * Vérifie si une salle existe à partir de son code.
     *
     * @param code code unique de la salle
     * @return Result contenant true si la salle existe, false sinon
     */
    suspend fun checkRoomExists(code: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/room-exists/$code")
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                response.use {
                    if (it.isSuccessful) {
                        val body = it.body?.string()
                        if (body != null) {
                            val existsResponse = moshi
                                .adapter(RoomExistsResponse::class.java)
                                .fromJson(body)

                            Result.success(existsResponse?.exists ?: false)
                        } else {
                            Result.failure(IOException("Empty response body"))
                        }
                    } else {
                        val errorBody = it.body?.string() ?: "Unknown error"
                        Result.failure(IOException("HTTP ${it.code}: $errorBody"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(IOException("Network error: ${e.message}", e))
            }
        }

    /**
     * Vérifie l’état de santé du backend.
     * Utilisé pour s’assurer que le serveur est accessible.
     *
     * @return Result contenant true si le serveur répond correctement
     */
    suspend fun checkHealth(): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/health")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                response.use {
                    Result.success(it.isSuccessful)
                }
            } catch (e: Exception) {
                Result.failure(IOException("Network error: ${e.message}", e))
            }
        }
}
