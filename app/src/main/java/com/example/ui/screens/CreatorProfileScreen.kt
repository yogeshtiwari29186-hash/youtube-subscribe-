package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.CreatorTopBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderGlow
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.YouTubeUtils

@Composable
fun CreatorProfileScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedCreator = viewModel.selectedCreator.collectAsState().value
    val user = selectedCreator ?: currentUser
    val isSelf = user?.userId == currentUser?.userId
    val myPromotions by viewModel.myPromotions.collectAsState()
    val context = LocalContext.current
    var isFollowing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = if (isSelf) "My Creator Profile" else (user?.username ?: "Creator"),
            showBack = !isSelf,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner & Avatar
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Profile Banner
                    AsyncImage(
                        model = R.drawable.creator_hero_banner_1791198233578,
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )

                    // Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DarkBackground.copy(alpha = 0.9f))
                                )
                            )
                    )

                    // Avatar
                    AsyncImage(
                        model = user?.profileImage.orEmpty().ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80" },
                        contentDescription = user?.username,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(start = 20.dp, top = 80.dp)
                            .size(84.dp)
                            .clip(CircleShape)
                            .border(3.dp, DiamondCyan, CircleShape)
                    )

                    // Top-right quick actions (Settings or Share)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSelf) {
                            Surface(
                                shape = CircleShape,
                                color = DarkSurfaceHighlight,
                                modifier = Modifier.clickable { viewModel.navigateTo(Screen.Settings) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                    tint = TextPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // User Info & Badges
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user?.username ?: "Creator",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified Creator",
                            tint = DiamondCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = user?.email.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user?.bio.orEmpty().ifEmpty { "Passionate content creator sharing original perspectives on YouTube and CreatorDiamond." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Community Statistics Row (Followers, Following, Diamonds)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column {
                            Text("${user?.followersCount ?: 0}", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Followers", fontSize = 11.sp, color = TextSecondary)
                        }
                        Column {
                            Text("${user?.followingCount ?: 0}", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Following", fontSize = 11.sp, color = TextSecondary)
                        }
                        Column {
                            Text("${user?.diamonds ?: 0} 💎", fontWeight = FontWeight.Bold, color = DiamondCyan)
                            Text("Diamonds", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons: Follow / Edit / Share / Channel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isSelf) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.EditProfile) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_profile_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DiamondCyan,
                                    contentColor = Color(0xFF0A0D14)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { isFollowing = !isFollowing },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) DarkSurfaceHighlight else DiamondCyan,
                                    contentColor = if (isFollowing) TextPrimary else Color(0xFF0A0D14)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isFollowing) "Following" else "Follow", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Open Official YouTube Channel
                        if (!user?.youtubeChannelUrl.isNullOrEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    YouTubeUtils.openOfficialYouTube(context, user.youtubeChannelUrl)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF0000)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF0000))
                            ) {
                                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Channel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Check out ${user?.username} on CreatorDiamond!")
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Creator Profile"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DarkBorder)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Creator Badges
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Creator Recognition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CreatorBadgeItem("💎 Diamond Elite", DiamondCyan, Modifier.weight(1f))
                        CreatorBadgeItem("✨ Verified", ElectricBlue, Modifier.weight(1f))
                        CreatorBadgeItem("🚀 Active Host", DiamondGold, Modifier.weight(1f))
                    }
                }
            }

            // Published Promotions section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Published Promotions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (myPromotions.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No promotions published yet.", color = TextSecondary)
                        }
                    }
                }
            } else {
                items(myPromotions) { promo ->
                    MyContentCard(
                        promo = promo,
                        onOpen = { YouTubeUtils.openOfficialYouTube(context, promo.targetUrl) },
                        onDetails = { viewModel.openContentDetails(promo) }
                    )
                }
            }
        }
    }
}

@Composable
fun CreatorBadgeItem(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun EditProfileScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var username by remember { mutableStateOf(currentUser?.username.orEmpty()) }
    var bio by remember { mutableStateOf(currentUser?.bio.orEmpty()) }
    var channelUrl by remember { mutableStateOf(currentUser?.youtubeChannelUrl.orEmpty()) }
    var channelId by remember { mutableStateOf(currentUser?.youtubeChannelId.orEmpty()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Edit Profile",
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
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Creator Display Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_username_input"),
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
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Creator Bio") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_bio_input"),
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
                OutlinedTextField(
                    value = channelUrl,
                    onValueChange = { channelUrl = it },
                    label = { Text("Official YouTube Channel URL") },
                    placeholder = { Text("https://www.youtube.com/@YourHandle") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_channel_url_input"),
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
                OutlinedTextField(
                    value = channelId,
                    onValueChange = { channelId = it },
                    label = { Text("YouTube Channel ID / Handle") },
                    placeholder = { Text("@YourHandle") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_channel_id_input"),
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
                        viewModel.updateProfile(username, bio, channelUrl, channelId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiamondCyan,
                        contentColor = Color(0xFF0A0D14)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
