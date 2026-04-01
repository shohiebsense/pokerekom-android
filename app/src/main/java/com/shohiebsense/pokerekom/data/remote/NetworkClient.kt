package com.shohiebsense.pokerekom.data.remote

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkClient @Inject constructor(
    private val client: OkHttpClient
) {
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun buildPostRequest(url: String, jsonBody: String?): Request {
        val body = (jsonBody ?: "").toRequestBody(JSON)
        return Request.Builder()
            .url(url)
            .post(body)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()
    }

    suspend fun execute(request: Request): Response {
        return client.newCall(request).await()
    }

    private suspend fun Call.await(): Response {
        return suspendCancellableCoroutine { cont ->
            enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (cont.isCancelled) return
                    cont.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    cont.resumeWith(Result.success(response))
                }
            })

            cont.invokeOnCancellation {
                try {
                    cancel()
                } catch (_: Throwable) {}
            }
        }
    }

    suspend fun downloadBytes(url: String): Pair<ByteArray, String?> {
        val req = Request.Builder().url(url).get().build()
        val resp = execute(req)
        resp.use {
            if (!it.isSuccessful) throw IOException("HTTP ${it.code}")
            val contentType = it.header("Content-Type")
            val bytes = it.body?.bytes() ?: throw IOException("empty body")
            return Pair(bytes, contentType)
        }
    }

    companion object {
        const val BASE_URL = "https://api.shohiebsense.io"
        const val RECOMMEND_ENDPOINT = "$BASE_URL/recommend"
        const val POKEMON_IMAGE_ENDPOINT = "$BASE_URL/pokemon"
    }
}
