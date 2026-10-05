package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderGlow
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGlow
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.Screen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DiamondBadge(
    diamonds: Long,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(diamonds)
    Surface(
        modifier = modifier
            .testTag("diamond_badge")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, DarkBorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Diamond,
                contentDescription = "Diamonds",
                tint = DiamondCyan,
                modifier = Modifier.size(18.dp)
            )
            AnimatedContent(
                targetState = formatted,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "diamond_counter"
            ) { count ->
                Text(
                    text = "$count 💎",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    borderGlow: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val borderBrush = if (borderGlow) {
        Brush.linearGradient(listOf(DiamondCyan.copy(alpha = 0.5f), ElectricBlue.copy(alpha = 0.2f)))
    } else {
        Brush.linearGradient(listOf(GlassBorder, DarkBorder))
    }

    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = BorderStroke(1.dp, borderBrush)
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorTopBar(
    title: String,
    showBack: Boolean = false,
    diamonds: Long = 0L,
    unreadNotifications: Int = 0,
    avatarUrl: String = "",
    onBackClick: () -> Unit = {},
    onDiamondClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground,
            titleContentColor = TextPrimary
        ),
        navigationIcon = {
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(DiamondCyan, ElectricBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Diamond,
                            contentDescription = "Logo",
                            tint = Color(0xFF0A0D14),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "CreatorDiamond",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }
        },
        title = {
            if (showBack) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            DiamondBadge(
                diamonds = diamonds,
                onClick = onDiamondClick,
                modifier = Modifier.padding(end = 4.dp)
            )

            BadgedBox(
                badge = {
                    if (unreadNotifications > 0) {
                        Badge(
                            containerColor = DiamondCyan,
                            contentColor = Color(0xFF0A0D14)
                        ) {
                            Text(text = "$unreadNotifications")
                        }
                    }
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.testTag("top_bar_notifications_button")
                ) {
                    Icon(
                        imageVector = if (unreadNotifications > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = if (unreadNotifications > 0) DiamondCyan else TextSecondary
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, DiamondCyan, CircleShape)
                    .clickable { onAvatarClick() }
                    .testTag("top_bar_avatar"),
                color = DarkSurfaceHighlight
            ) {
                if (avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile Avatar",
                        tint = DiamondCyan,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun ComplianceNoticeCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131F33),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Compliance",
                tint = DiamondCyan,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "YouTube API Compliant Community",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DiamondCyan
                )
                Text(
                    text = "No artificial engagement. We do not reward or incentivize YouTube views, likes, or subscriptions. All external content opens exclusively via official YouTube destinations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun CreatorBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val isHome = currentScreen == Screen.Home
    val isEarn = currentScreen == Screen.Earn
    val isWallet = currentScreen == Screen.Wallet
    val isProfile = currentScreen == Screen.CreatorProfile || currentScreen == Screen.Settings

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            containerColor = DarkSurface,
            contentColor = TextPrimary,
            tonalElevation = 8.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, DarkBorder))
        ) {
            NavigationBarItem(
                selected = isHome,
                onClick = { onNavigate(Screen.Home) },
                icon = {
                    Icon(
                        imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = { Text("HOME", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DiamondCyan,
                    selectedTextColor = DiamondCyan,
                    indicatorColor = DarkSurfaceVariant,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_home")
            )

            NavigationBarItem(
                selected = isEarn,
                onClick = { onNavigate(Screen.Earn) },
                icon = {
                    Icon(
                        imageVector = if (isEarn) Icons.Filled.Diamond else Icons.Outlined.Diamond,
                        contentDescription = "Earn"
                    )
                },
                label = { Text("EARN", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DiamondCyan,
                    selectedTextColor = DiamondCyan,
                    indicatorColor = DarkSurfaceVariant,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_earn")
            )

            // Center Spacer for elevated Create FAB
            Spacer(modifier = Modifier.weight(1f))

            NavigationBarItem(
                selected = isWallet,
                onClick = { onNavigate(Screen.Wallet) },
                icon = {
                    Icon(
                        imageVector = if (isWallet) Icons.Filled.Wallet else Icons.Outlined.Wallet,
                        contentDescription = "Wallet"
                    )
                },
                label = { Text("WALLET", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DiamondCyan,
                    selectedTextColor = DiamondCyan,
                    indicatorColor = DarkSurfaceVariant,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_wallet")
            )

            NavigationBarItem(
                selected = isProfile,
                onClick = { onNavigate(Screen.CreatorProfile) },
                icon = {
                    Icon(
                        imageVector = if (isProfile) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = { Text("PROFILE", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DiamondCyan,
                    selectedTextColor = DiamondCyan,
                    indicatorColor = DarkSurfaceVariant,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_profile")
            )
        }

        // Distinct Elevated CREATE Action FAB
        FloatingActionButton(
            onClick = { onNavigate(Screen.SelectPromotionType) },
            containerColor = DiamondCyan,
            contentColor = Color(0xFF0A0D14),
            shape = CircleShape,
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(56.dp)
                .shadow(12.dp, CircleShape, spotColor = DiamondCyan)
                .border(2.dp, Color(0xFF99F4FF), CircleShape)
                .testTag("nav_create_fab")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create Promotion",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
