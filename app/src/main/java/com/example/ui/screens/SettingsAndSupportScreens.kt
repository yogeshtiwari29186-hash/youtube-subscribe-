package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import com.example.ui.components.ComplianceNoticeCard
import com.example.ui.components.CreatorTopBar
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Notifications",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${notifications.size} Alerts",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Text(
                text = "Mark all as read",
                style = MaterialTheme.typography.labelMedium,
                color = DiamondCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { viewModel.markNotificationsAsRead() }
                    .padding(4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No notifications at this time.", color = TextSecondary)
                        }
                    }
                }
            } else {
                items(notifications) { notif ->
                    NotificationItemCard(notif = notif)
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(notif: AppNotification) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(notif.timestamp))
    val (icon, color) = when (notif.type) {
        NotificationType.PROMOTION_APPROVED -> Icons.Filled.Campaign to SuccessGreen
        NotificationType.PROMOTION_REJECTED -> Icons.Filled.Campaign to ErrorRed
        NotificationType.PROMOTION_COMPLETED -> Icons.Filled.Check to DiamondCyan
        NotificationType.DIAMOND_EARNED -> Icons.Filled.Diamond to DiamondCyan
        NotificationType.DIAMOND_SPENT -> Icons.Filled.Diamond to ElectricBlue
        NotificationType.SECURITY_ALERT -> Icons.Filled.Security to WarningAmber
        NotificationType.SYSTEM_ANNOUNCEMENT -> Icons.Filled.Star to DiamondGold
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${notif.notificationId}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notif.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notif.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    var earnNotifs by remember { mutableStateOf(true) }
    var promoNotifs by remember { mutableStateOf(true) }
    var highContrastDark by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Settings",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account info card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Account Identity", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(user?.username ?: "Creator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = DiamondCyan)
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceHighlight
                        ) {
                            Text(
                                text = "Unified Viewer + Creator Architecture",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Admin Dashboard access
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_admin_panel_card"),
                    borderGlow = true,
                    onClick = { viewModel.navigateTo(Screen.AdminPanel) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DiamondGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = DiamondGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin Control Dashboard", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Manage economy parameters, users, audit logs, and fraud rules.", fontSize = 11.sp, color = TextSecondary)
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Notification preferences
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notification Preferences", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Reward & Earn Alerts", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("Alerts when task diamonds are credited", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = earnNotifs,
                                onCheckedChange = { earnNotifs = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DiamondCyan, checkedTrackColor = DarkSurfaceHighlight)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Campaign Status Updates", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("Alerts when promotions launch or finish", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = promoNotifs,
                                onCheckedChange = { promoNotifs = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DiamondCyan, checkedTrackColor = DarkSurfaceHighlight)
                            )
                        }
                    }
                }
            }

            // Legal & Information items
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsNavRow("Help & Support / FAQ", Icons.AutoMirrored.Filled.Help) {
                            viewModel.navigateTo(Screen.HelpSupport)
                        }
                        SettingsNavRow("Terms of Service", Icons.Filled.Description) {
                            viewModel.navigateTo(Screen.Terms)
                        }
                        SettingsNavRow("Privacy Policy", Icons.Filled.PrivacyTip) {
                            viewModel.navigateTo(Screen.PrivacyPolicy)
                        }
                    }
                }
            }

            // Logout & Session
            item {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button"),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Logout Session", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsNavRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = DiamondCyan, modifier = Modifier.size(20.dp))
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun HelpSupportScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    val faqs = listOf(
        "How do Diamonds work?" to "Diamonds are internal creator community tokens. You earn diamonds exclusively through legitimate in-app activities (such as daily logins, profile setup, and community quizzes) and can spend them to feature your YouTube channel or original videos in the community discovery feed.",
        "Are YouTube views or subscribers guaranteed?" to "Absolutely not. In strict compliance with YouTube policies and Developer Terms of Service, CreatorDiamond never guarantees, sells, trades, or artificially generates views, likes, comments, or subscribers. Promotions merely display your link to other creators for voluntary discovery.",
        "Do you reward users for watching YouTube videos?" to "No. We never award diamonds for watching videos for any length of time, subscribing to channels, or liking videos. In-app tasks are strictly limited to independent in-app activities.",
        "Can I get a refund on spent diamonds?" to "Once a campaign is approved and active in the discovery feed, diamonds are allocated to impression delivery. Inactive or remaining budgets can be cancelled via My Content.",
        "How is fraud prevented?" to "Our backend enforces daily rate limits, atomic ledger transactions, and anti-abuse safeguards to prevent duplicate claims and balance manipulation."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Help & Support",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ComplianceNoticeCard()
            }

            item {
                Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(faqs) { (q, a) ->
                var expanded by remember { mutableStateOf(false) }
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = q, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = DiamondCyan
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = a, style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermsScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Terms of Service",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("1. Acceptance of Terms", fontWeight = FontWeight.Bold, color = DiamondCyan)
                        Text(
                            "By accessing or using CreatorDiamond, you agree to be bound by these Terms. CreatorDiamond is a community discovery platform designed for independent creators to discover and share original video content.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Text("2. YouTube API Services Compliance", fontWeight = FontWeight.Bold, color = DiamondCyan)
                        Text(
                            "CreatorDiamond operates in strict accordance with the YouTube API Services Terms of Service and Developer Policies. Users acknowledge that: (a) this application does not sell, trade, or artificially incentivize YouTube views, likes, subscribers, or comments; (b) diamonds are in-app non-monetary community tokens awarded solely for legitimate in-app achievements; (c) third-party content is accessed exclusively through official YouTube URLs and players.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Text("3. Unified Creator-Viewer Identity", fontWeight = FontWeight.Bold, color = DiamondCyan)
                        Text(
                            "All user accounts in CreatorDiamond possess unified creator and viewer capabilities. There are no segregated tier accounts. Any user may discover content and promote their own verified content.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Text("4. Diamond Token Economy", fontWeight = FontWeight.Bold, color = DiamondCyan)
                        Text(
                            "Diamonds have no cash, monetary, or redeemable value outside of CreatorDiamond. Balance manipulation, exploitation of task bugs, or reverse engineering will result in immediate account termination.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        CreatorTopBar(
            title = "Privacy Policy",
            showBack = true,
            onBackClick = { viewModel.navigateBack() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Privacy & Data Stewardship", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            "We respect creator privacy. CreatorDiamond collects only minimal account information required to provide discovery services, including display name, email, and public YouTube channel identifiers. We never sell your personal information or track your private activity across external services.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Text("Security & Auditing", fontWeight = FontWeight.Bold, color = DiamondCyan)
                        Text(
                            "All diamond ledger movements are recorded in tamper-resistant server transaction logs to prevent unauthorized account modifications and ensure community fairness.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
