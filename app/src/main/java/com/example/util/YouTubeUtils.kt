package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.regex.Pattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class ParsedYouTubeVideo(
    val videoId: String,
    val canonicalUrl: String,
    val thumbnailUrl: String
)

data class ChannelPreview(
    val channelIdentifier: String,
    val canonicalUrl: String,
    val channelName: String,
    val thumbnailUrl: String
)

data class ParsedYouTubeChannel(
    val channelIdentifier: String,
    val isHandle: Boolean,
    val canonicalUrl: String,
    val avatarUrl: String
)

object YouTubeUtils {
    // Official video pattern matching standard watch URLs, shortlinks, and shorts
    private val VIDEO_ID_PATTERNS = listOf(
        Pattern.compile("(?:v=|/v/|embed/|shorts/|youtu\\.be/)([a-zA-Z0-9_-]{11})"),
        Pattern.compile("^([a-zA-Z0-9_-]{11})$")
    )

    // Channel pattern matching handles and channel IDs
    private val CHANNEL_HANDLE_PATTERN = Pattern.compile("(?:youtube\\.com/(?:@|c/))([a-zA-Z0-9_.-]+)")
    private val CHANNEL_ID_PATTERN = Pattern.compile("youtube\\.com/channel/(UC[a-zA-Z0-9_-]{22})")

    fun parseVideoUrl(input: String): ParsedYouTubeVideo? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        for (pattern in VIDEO_ID_PATTERNS) {
            val matcher = pattern.matcher(trimmed)
            if (matcher.find()) {
                val videoId = matcher.group(1) ?: continue
                if (videoId.length == 11) {
                    return ParsedYouTubeVideo(
                        videoId = videoId,
                        canonicalUrl = "https://www.youtube.com/watch?v=$videoId",
                        thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                    )
                }
            }
        }
        return null
    }

    fun parseChannelUrl(input: String): ParsedYouTubeChannel? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val handleMatcher = CHANNEL_HANDLE_PATTERN.matcher(trimmed)
        if (handleMatcher.find()) {
            val handle = handleMatcher.group(1) ?: return null
            val cleanHandle = if (handle.startsWith("@")) handle else "@$handle"
            return ParsedYouTubeChannel(
                channelIdentifier = cleanHandle,
                isHandle = true,
                canonicalUrl = "https://www.youtube.com/$cleanHandle",
                avatarUrl = "https://ui-avatars.com/api/?name=${cleanHandle.removePrefix("@")}&background=00E5FF&color=0A0D14"
            )
        }

        val idMatcher = CHANNEL_ID_PATTERN.matcher(trimmed)
        if (idMatcher.find()) {
            val channelId = idMatcher.group(1) ?: return null
            return ParsedYouTubeChannel(
                channelIdentifier = channelId,
                isHandle = false,
                canonicalUrl = "https://www.youtube.com/channel/$channelId",
                avatarUrl = "https://ui-avatars.com/api/?name=Creator&background=2979FF&color=FFFFFF"
            )
        }

        // Also accept raw handle directly e.g. @CreatorHub
        if (trimmed.startsWith("@") && trimmed.length >= 3) {
            return ParsedYouTubeChannel(
                channelIdentifier = trimmed,
                isHandle = true,
                canonicalUrl = "https://www.youtube.com/$trimmed",
                avatarUrl = "https://ui-avatars.com/api/?name=${trimmed.removePrefix("@")}&background=00E5FF&color=0A0D14"
            )
        }

        return null
    }


    suspend fun fetchChannelPreview(inputUrl: String): ChannelPreview? = withContext(Dispatchers.IO) {
        try {
            val parsed = parseChannelUrl(inputUrl) ?: return@withContext null
            val connection = (URL(parsed.canonicalUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("User-Agent", "Mozilla/5.0")
                instanceFollowRedirects = true
            }
            val html = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            fun meta(property: String): String? {
                val p1 = Regex("""<meta[^>]+(?:property|name)=["']${Regex.escape(property)}["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                val p2 = Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+(?:property|name)=["']${Regex.escape(property)}["']""", RegexOption.IGNORE_CASE)
                return (p1.find(html)?.groupValues?.getOrNull(1) ?: p2.find(html)?.groupValues?.getOrNull(1))
                    ?.replace("&amp;", "&")
            }

            val title = meta("og:title") ?: parsed.channelIdentifier
            val image = meta("og:image") ?: parsed.avatarUrl
            ChannelPreview(parsed.channelIdentifier, parsed.canonicalUrl, title, image)
        } catch (_: Exception) {
            val parsed = parseChannelUrl(inputUrl) ?: return@withContext null
            ChannelPreview(parsed.channelIdentifier, parsed.canonicalUrl, parsed.channelIdentifier, parsed.avatarUrl)
        }
    }

    /**
     * Opens official YouTube app or falls back to system browser.
     * Complies with YouTube policy by directing the viewer to the official destination.
     */
    fun openOfficialYouTube(context: Context, url: String) {
        val uri = Uri.parse(url)
        try {
            val youtubeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.youtube")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(youtubeIntent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
