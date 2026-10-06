package com.example.util

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object YouTubeSubscriptionVerifier {
    const val YOUTUBE_READONLY_SCOPE = "https://www.googleapis.com/auth/youtube.readonly"
    private val client = OkHttpClient()

    fun requestAuthorization(context: Context) =
        Identity.getAuthorizationClient(context).authorize(
            AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(YOUTUBE_READONLY_SCOPE)))
                // Always let the user explicitly choose the Google account.
                .setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
                .build()
        )

    suspend fun isSubscribed(
        accessToken: String,
        targetVideoId: String? = null,
        targetChannelId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val channelId = targetChannelId?.let { resolveChannelId(accessToken, it) }
            ?: targetVideoId?.let { resolveVideoChannelId(accessToken, it) }
            ?: return@withContext false

        val encoded = java.net.URLEncoder.encode(channelId, "UTF-8")
        val url = "https://www.googleapis.com/youtube/v3/subscriptions?part=snippet&mine=true&forChannelId=$encoded&maxResults=1"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext false
            val body = response.body?.string().orEmpty()
            JSONObject(body).optInt("totalResults", 0) > 0
        }
    }

    private fun resolveChannelId(accessToken: String, value: String): String? {
        if (value.startsWith("UC")) return value

        val handle = value.removePrefix("@")
        val encoded = java.net.URLEncoder.encode("@$handle", "UTF-8")
        val url = "https://www.googleapis.com/youtube/v3/channels?part=id&forHandle=$encoded"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use null
            val body = response.body?.string().orEmpty()
            val items = JSONObject(body).optJSONArray("items") ?: return@use null
            if (items.length() == 0) return@use null
            val item = items.getJSONObject(0)
            item.optString("id").takeIf { it.isNotBlank() }
        }
    }

    private fun resolveVideoChannelId(accessToken: String, videoId: String): String? {
        val encoded = java.net.URLEncoder.encode(videoId, "UTF-8")
        val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$encoded"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use null
            val body = response.body?.string().orEmpty()
            val items = JSONObject(body).optJSONArray("items") ?: return@use null
            if (items.length() == 0) return@use null

            val item = items.getJSONObject(0)
            val snippet = item.optJSONObject("snippet") ?: return@use null
            snippet.optString("channelId").takeIf { it.isNotBlank() }
        }
    }
}
