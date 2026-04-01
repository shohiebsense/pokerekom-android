package com.shohiebsense.pokerekom.data.repository

import com.shohiebsense.pokerekom.data.local.ImageCache
import com.shohiebsense.pokerekom.data.local.PrefsDataStore
import com.shohiebsense.pokerekom.data.remote.NetworkClient
import com.shohiebsense.pokerekom.domain.model.TeamResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

interface PokemonRepository {
    suspend fun fetchTeam(payloadJson: String? = "{}"): List<String>
    suspend fun ensureImagesCached(team: List<String>, maxRetries: Int = 3)
    suspend fun loadCachedTeam(): List<String>?
}

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val networkClient: NetworkClient,
    private val imageCache: ImageCache,
    private val prefsDataStore: PrefsDataStore,
    private val gson: Gson
) : PokemonRepository {

    override suspend fun fetchTeam(payloadJson: String?): List<String> = withContext(Dispatchers.IO) {
        val request = networkClient.buildPostRequest(
            NetworkClient.RECOMMEND_ENDPOINT,
            payloadJson
        )
        val response = try {
            networkClient.execute(request)
        } catch (e: IOException) {
            throw e
        }

        response.use {
            if (!it.isSuccessful) throw IOException("HTTP ${it.code}")
            val body = it.body?.string().orEmpty()
            if (body.isBlank()) throw IOException("empty response")

            val parsed = try {
                gson.fromJson(body, TeamResponse::class.java)
            } catch (e: Exception) {
                throw e
            }
            val teamLine = parsed.teamLine ?: ""
            prefsDataStore.saveLastTeamJson(body)

            teamLine.split(",").mapNotNull {
                val n = it.trim()
                if (n.isEmpty()) null else n
            }
        }
    }

    override suspend fun ensureImagesCached(team: List<String>, maxRetries: Int) = withContext(Dispatchers.IO) {
        for (name in team) {
            val lowerName = name.trim().lowercase()
            if (imageCache.getCachedFile(lowerName) != null) continue

            val encoded = URLEncoder.encode(lowerName, "utf-8")
            val imageUrl = "${NetworkClient.POKEMON_IMAGE_ENDPOINT}/$encoded"

            var attempt = 0
            var success = false
            while (attempt < maxRetries && !success) {
                attempt++
                try {
                    val (bytes, contentType) = networkClient.downloadBytes(imageUrl)
                    imageCache.saveImage(lowerName, bytes, contentType)
                    success = true
                } catch (e: IOException) {
                    val jitter = Random.nextLong(0, 300L)
                    val backoff = (300L * (1 shl (attempt - 1))).coerceAtMost(5000L) + jitter
                    delay(backoff)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    override suspend fun loadCachedTeam(): List<String>? {
        val json = prefsDataStore.getLastTeamJson() ?: return null
        return try {
            val parsed = gson.fromJson(json, TeamResponse::class.java)
            parsed.teamLine?.split(",")?.mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() } }
        } catch (e: Exception) {
            null
        }
    }
}
