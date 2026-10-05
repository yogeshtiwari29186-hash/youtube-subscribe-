package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.util.YouTubeUtils
import kotlinx.coroutines.delay

@Composable
fun ChannelSetupScreen(viewModel: CreatorDiamondViewModel, modifier: Modifier = Modifier) {
    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<com.example.util.ChannelPreview?>(null) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(url) {
        if (url.length > 8) {
            delay(500)
            loading = true
            preview = YouTubeUtils.fetchChannelPreview(url)
            loading = false
            if (preview == null) error = "Enter a valid public YouTube channel link."
            else error = ""
        } else {
            preview = null
            error = ""
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(DarkBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(36.dp))
        Text("Connect your YouTube channel", color = TextPrimary, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text("Add your channel link. We will load its name and thumbnail before you confirm.", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("YouTube Channel Link") },
            placeholder = { Text("https://www.youtube.com/@YourChannel") },
            leadingIcon = { Icon(Icons.Filled.Link, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(18.dp))
        if (preview != null) {
            AsyncImage(
                model = preview!!.thumbnailUrl,
                contentDescription = preview!!.channelName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(96.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(10.dp))
            Text(preview!!.channelName, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(preview!!.channelIdentifier, color = DiamondCyan, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            Text("Is this your channel?", color = TextSecondary)
        } else if (loading) {
            Text("Loading channel…", color = DiamondCyan)
        } else if (error.isNotBlank()) {
            Text(error, color = Color(0xFFFF6B6B), fontSize = 13.sp)
        }
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = {
                preview?.let {
                    viewModel.confirmChannelSetup(it.channelName, it.canonicalUrl, it.channelIdentifier, it.thumbnailUrl)
                }
            },
            enabled = preview != null && !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan, contentColor = Color(0xFF0A0D14)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Confirm & Continue", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text("🎁 Your new account starts with 200 💎", color = DiamondGold, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("The channel thumbnail will be used as your profile image.", color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun QuickVideoPromotionScreen(viewModel: CreatorDiamondViewModel, modifier: Modifier = Modifier) {
    var url by remember { mutableStateOf("") }
    val parsed = remember(url) { YouTubeUtils.parseVideoUrl(url) }
    Column(
        modifier = modifier.fillMaxSize().background(DarkBackground).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Filled.PlayArrow, null, tint = DiamondCyan, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(16.dp))
        Text("Add your first video", color = TextPrimary, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text("Paste a YouTube video link to promote it in the community.", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("YouTube Video Link") },
            placeholder = { Text("https://youtu.be/...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(16.dp))
        parsed?.let {
            AsyncImage(
                model = it.thumbnailUrl,
                contentDescription = "Video thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(14.dp))
            )
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { viewModel.createQuickVideoPromotion(url) },
            enabled = parsed != null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan, contentColor = Color(0xFF0A0D14)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Promote Video • 100 💎", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text("You have 200 💎 free on a new account.", color = DiamondGold, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        TextButtonSkip { viewModel.navigateTo(com.example.ui.viewmodel.Screen.Home) }
    }
}

@Composable
private fun TextButtonSkip(onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text("Skip for now", color = TextSecondary)
    }
}
