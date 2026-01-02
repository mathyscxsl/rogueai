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

class RoomsApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val baseUrl = "https://backend.rogueai.surpuissant.io"

    suspend fun createRoom(soloGame: Boolean = false): Result<CreateRoomResponse> = withContext(Dispatchers.IO) {
        try {
            val requestData = CreateRoomRequest(soloGame = soloGame)
            val json = moshi.adapter(CreateRoomRequest::class.java).toJson(requestData)

            val request = Request.Builder()
                .url("$baseUrl/create-room")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            response.use {
                if (it.isSuccessful) {
                    val body = it.body?.string()
                    if (body != null) {
                        val roomResponse = moshi.adapter(CreateRoomResponse::class.java).fromJson(body)
                        if (roomResponse != null) {
                            Result.success(roomResponse)
                        } else {
                            Result.failure(IOException("Invalid response format"))
                        }
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

    suspend fun checkRoomExists(code: String): Result<Boolean> = withContext(Dispatchers.IO) {
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
                        val existsResponse = moshi.adapter(RoomExistsResponse::class.java).fromJson(body)
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

    suspend fun checkHealth(): Result<Boolean> = withContext(Dispatchers.IO) {
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