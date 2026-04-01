package com.shohiebsense.pokerekom

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shohiebsense.pokerekom.data.local.PrefsDataStore
import com.shohiebsense.pokerekom.data.remote.NetworkClient
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RecommendUploadWorker @Inject constructor(
    @ApplicationContext ctx: Context,
    params: WorkerParameters,
    private val networkClient: NetworkClient
) : CoroutineWorker(ctx, params) {

    private val prefs = PrefsDataStore(ctx)
    private val gson = Gson()

    override suspend fun doWork(): Result {
        val cachedPayload = inputData.getString("payload_json") ?: "{}"
        val url = "https://api.shohiebsense.io/recommend"
        val request = networkClient.buildPostRequest(url, cachedPayload)
        return try {
            val resp = networkClient.execute(request)
            if (resp.isSuccessful) {
                val body = resp.body?.string()
                body?.let {
                    prefs.saveLastTeamJson(it)
                }
                resp.close()
                Result.success()
            } else {
                resp.close()
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
