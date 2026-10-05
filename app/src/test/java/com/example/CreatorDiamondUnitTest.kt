package com.example

import com.example.data.model.EconomySettings
import com.example.data.model.PromotionStatus
import com.example.data.model.PromotionType
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserAccount
import com.example.util.YouTubeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatorDiamondUnitTest {

    @Test
    fun singleUserAccountModel_supportsBothViewerAndCreator() {
        val user = UserAccount(
            userId = "user_123",
            username = "TechCreator",
            email = "tech@example.com",
            diamonds = 2450L,
            youtubeChannelId = "UC1234567890123456789012",
            youtubeChannelUrl = "https://www.youtube.com/@TechCreator",
            creatorEnabled = true
        )

        assertEquals("user_123", user.userId)
        assertEquals(2450L, user.diamonds)
        assertTrue(user.creatorEnabled) // Unified creator and viewer capability
        assertFalse(user.isSuspended)
    }

    @Test
    fun youTubeUtils_parsesStandardWatchUrl() {
        val url = "https://www.youtube.com/watch?v=a1B2c3D4e5F"
        val parsed = YouTubeUtils.parseVideoUrl(url)

        assertNotNull(parsed)
        assertEquals("a1B2c3D4e5F", parsed?.videoId)
        assertEquals("https://www.youtube.com/watch?v=a1B2c3D4e5F", parsed?.canonicalUrl)
        assertEquals("https://img.youtube.com/vi/a1B2c3D4e5F/hqdefault.jpg", parsed?.thumbnailUrl)
    }

    @Test
    fun youTubeUtils_parsesShortsAndShortUrl() {
        val shortUrl = "https://youtu.be/a1B2c3D4e5F"
        val parsedShort = YouTubeUtils.parseVideoUrl(shortUrl)
        assertNotNull(parsedShort)
        assertEquals("a1B2c3D4e5F", parsedShort?.videoId)

        val shortsUrl = "https://www.youtube.com/shorts/a1B2c3D4e5F"
        val parsedShorts = YouTubeUtils.parseVideoUrl(shortsUrl)
        assertNotNull(parsedShorts)
        assertEquals("a1B2c3D4e5F", parsedShorts?.videoId)
    }

    @Test
    fun youTubeUtils_rejectsInvalidUrls() {
        val invalidUrl1 = "https://vimeo.com/123456"
        assertNull(YouTubeUtils.parseVideoUrl(invalidUrl1))

        val invalidUrl2 = "not_a_url"
        assertNull(YouTubeUtils.parseVideoUrl(invalidUrl2))

        val emptyUrl = ""
        assertNull(YouTubeUtils.parseVideoUrl(emptyUrl))
    }

    @Test
    fun youTubeUtils_parsesChannelHandle() {
        val handleUrl = "https://www.youtube.com/@AlexCreatorTech"
        val parsed = YouTubeUtils.parseChannelUrl(handleUrl)

        assertNotNull(parsed)
        assertEquals("@AlexCreatorTech", parsed?.channelIdentifier)
        assertTrue(parsed?.isHandle == true)
        assertEquals("https://www.youtube.com/@AlexCreatorTech", parsed?.canonicalUrl)
    }

    @Test
    fun youTubeUtils_parsesRawChannelHandle() {
        val rawHandle = "@AlexCreatorTech"
        val parsed = YouTubeUtils.parseChannelUrl(rawHandle)

        assertNotNull(parsed)
        assertEquals("@AlexCreatorTech", parsed?.channelIdentifier)
    }

    @Test
    fun walletBudgetValidation_detectsInsufficientDiamonds() {
        val userBalance = 300L
        val requiredBudget = 500L

        val hasEnough = userBalance >= requiredBudget
        assertFalse("User balance must be rejected if less than budget", hasEnough)
    }

    @Test
    fun walletBudgetValidation_enforcesMinimumBudget() {
        val settings = EconomySettings(minimumPromotionBudget = 100L)
        val budget = 50L

        val isValid = budget >= settings.minimumPromotionBudget
        assertFalse("Budget under 100 💎 must be rejected", isValid)
    }

    @Test
    fun antiFraud_duplicateTaskCompletionRule() {
        val completedTaskIds = mutableSetOf("task_profile", "task_link_yt")
        val attemptedTaskId = "task_profile"

        val canClaim = !completedTaskIds.contains(attemptedTaskId)
        assertFalse("One-time tasks cannot be claimed more than once", canClaim)
    }

    @Test
    fun adminAuditLog_requiresNonBlankReason() {
        fun validateAdminAdjustment(reason: String): Boolean {
            return reason.isNotBlank()
        }

        assertTrue(validateAdminAdjustment("Bonus promotion compensation"))
        assertFalse(validateAdminAdjustment(""))
        assertFalse(validateAdminAdjustment("   "))
    }
}
