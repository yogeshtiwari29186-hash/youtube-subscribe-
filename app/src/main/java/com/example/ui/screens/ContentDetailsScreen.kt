package com.example.ui.screens

import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.example.data.model.PromotionType
import com.example.ui.components.ComplianceNoticeCard
import com.example.ui.components.CreatorTopBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderGlow
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.YouTubeUtils

private const val REQUIRED_WATCH_SECONDS = 50

private class YouTubePlaybackBridge(private val onStateChanged: (Int) -> Unit) {
    @JavascriptInterface
    fun onPlayerState(state: Int) {
        onStateChanged(state)
    }
}

private fun youtubeEmbedHtml(videoId: String): String = """
<!doctype html>
<html>
<head>
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1">
<style>
html,body,#player{margin:0;padding:0;width:100%;height:100%;background:#000;overflow:hidden}
</style>
</head>
<body>
<div id="player"></div>
<script>
var player;
var tag=document.createElement('script');
tag.src='https://www.youtube.com/iframe_api';
document.head.appendChild(tag);

function onYouTubeIframeAPIReady() {
  player = new YT.Player('player', {
    videoId: 'VIDEO_ID',
    width: '100%',
    height: '100%',
    playerVars: {
      autoplay: 0,
      controls: 1,
      playsinline: 1,
      rel: 0,
      enablejsapi: 1,
      origin: 'https://www.youtube.com'
    },
    events: {
      onReady: function() {
        if (window.AndroidPlayback) AndroidPlayback.onPlayerState(-1);
      },
      onStateChange: function(e) {
        if (window.AndroidPlayback) AndroidPlayback.onPlayerState(e.data);
      },
      onError: function(e) {
        if (window.AndroidPlayback) AndroidPlayback.onPlayerState(-100 - e.data);
      }
    }
  });
}
</script>
</body>
</html>
""".replace("VIDEO_ID", videoId)
@Composable
fun ContentDetailsScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val promo = viewModel.selectedPromotion.collectAsState().value
    val context = LocalContext.current
    var watchSeconds by remember(promo?.promotionId) { mutableStateOf(0) }
    var isVideoPlaying by remember(promo?.promotionId) { mutableStateOf(false) }
    val videoId = remember(promo?.promotionId, promo?.targetUrl) {
        promo?.let { YouTubeUtils.parseVideoUrl(it.targetUrl)?.videoId }
    }
    val previewFinished = watchSeconds >= REQUIRED_WATCH_SECONDS

    LaunchedEffect(promo?.promotionId, isVideoPlaying) {
        if (promo?.type == PromotionType.YOUTUBE_VIDEO && isVideoPlaying && !previewFinished) {
            while (watchSeconds < REQUIRED_WATCH_SECONDS && isVideoPlaying) {
                delay(1000)
                if (isVideoPlaying) watchSeconds = (watchSeconds + 1).coerceAtMost(REQUIRED_WATCH_SECONDS)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Content Details",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        if (promo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No promotion selected.", color = TextSecondary)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (promo.type == PromotionType.YOUTUBE_VIDEO && videoId != null) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, DarkBorderGlow, RoundedCornerShape(18.dp)),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = true
                                    settings.loadsImagesAutomatically = true
                                    settings.allowContentAccess = true
                                    settings.userAgentString =
                                        settings.userAgentString + " YouTubeAndroidPlayer"
                                    webViewClient = WebViewClient()
                                    addJavascriptInterface(
                                        YouTubePlaybackBridge { state ->
                                            isVideoPlaying = state == 1
                                        },
                                        "AndroidPlayback"
                                    )
                                    loadDataWithBaseURL(
                                        "https://www.youtube.com",
                                        youtubeEmbedHtml(videoId),
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            update = { }
                        )
                    } else {
                        AsyncImage(
                            model = promo.thumbnailUrl,
                            contentDescription = promo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }

                    Text(
                        text = if (previewFinished)
                            "50-second watch completed ✓"
                        else
                            "Watch in the app: " + watchSeconds + "s / " + REQUIRED_WATCH_SECONDS + "s",
                        color = if (previewFinished) Color(0xFF55E39A) else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Title & Badges
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = DiamondCyan,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PROMOTED CAMPAIGN",
                                color = Color(0xFF0A0D14),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "· ${promo.impressions} community impressions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = promo.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }

            // Creator Row
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = promo.creatorAvatar,
                            contentDescription = promo.creatorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, DiamondCyan, CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = promo.creatorName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = promo.targetId,
                                style = MaterialTheme.typography.bodySmall,
                                color = DiamondCyan
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, promo.title)
                                    putExtra(Intent.EXTRA_TEXT, "${promo.title}\n${promo.targetUrl}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Creator Content"))
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, DarkBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Description
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About this content",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = promo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Watch-progress status and official YouTube action.
            if (promo.type == PromotionType.YOUTUBE_VIDEO) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (previewFinished) "50-second watch complete" else "Watch the video in the app",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (previewFinished) {
                                    "You can continue on the official YouTube channel."
                                } else {
                                    "Watched: " + watchSeconds + " / " + REQUIRED_WATCH_SECONDS + " seconds"
                                },
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { YouTubeUtils.openOfficialYouTube(context, promo.targetUrl) },
                    enabled = promo.type != PromotionType.YOUTUBE_VIDEO || previewFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("details_watch_official_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF4A1A1A),
                        disabledContentColor = Color(0xFFB8B8B8)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (promo.type == PromotionType.YOUTUBE_VIDEO && !previewFinished) {
                            "Watch 50s to unlock YouTube"
                        } else {
                            "Open Official YouTube ↗"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Report and Compliance
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.ReportContent) },
                        border = BorderStroke(1.dp, DarkBorder),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.testTag("report_content_btn")
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Report this content", fontSize = 12.sp)
                    }
                }
            }

            item {
                ComplianceNoticeCard()
            }
        }
    }
}

@Composable
fun ReportContentScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val promo = viewModel.selectedPromotion.collectAsState().value
    var selectedReason by remember { mutableStateOf("Inappropriate content") }
    var details by remember { mutableStateOf("") }

    val reasons = listOf(
        "Inappropriate content",
        "Broken or misleading link",
        "Spam or duplicate promotion",
        "Impersonation or copyright",
        "Terms violation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Report Content",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Submit a Moderation Report",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Target: ${promo?.title ?: "Content"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiamondCyan
                )
            }

            item {
                Text("Select Violation Category:", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reasons.forEach { reason ->
                        FilterChip(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            label = { Text(reason) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiamondCyan,
                                selectedLabelColor = Color(0xFF0A0D14),
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Additional Details (Optional)") },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_details_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        viewModel.submitReport(selectedReason, details)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_report_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Report to Moderators", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
