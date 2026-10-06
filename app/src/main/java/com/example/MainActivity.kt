package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.remote.FirebaseCloudSync
import com.example.data.repository.CreatorDiamondRepository
import com.example.ui.components.CreatorBottomNavigation
import com.example.ui.screens.AddYouTubeChannelScreen
import com.example.ui.screens.AddYouTubeVideoScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.ContentDetailsScreen
import com.example.ui.screens.ChannelSetupScreen
import com.example.ui.screens.QuickVideoPromotionScreen
import com.example.ui.screens.ContentFeedScreen
import com.example.ui.screens.CreatorProfileScreen
import com.example.ui.screens.EarnScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.HelpSupportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MyContentScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.PromotionBudgetScreen
import com.example.ui.screens.PromotionConfirmationScreen
import com.example.ui.screens.PromotionSuccessScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ReportContentScreen
import com.example.ui.screens.SelectPromotionTypeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TaskDetailsScreen
import com.example.ui.screens.TermsScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.CreatorDiamondViewModelFactory
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CreatorDiamondRepository(database)
        val viewModelFactory = CreatorDiamondViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: CreatorDiamondViewModel = viewModel(factory = viewModelFactory)
                CreatorDiamondApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CreatorDiamondApp(
    viewModel: CreatorDiamondViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI events for snackbar messages
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Mirror important local app state to Firebase Realtime Database.
    LaunchedEffect(Unit) {
        launch { viewModel.currentUser.collectLatest { it?.let(FirebaseCloudSync::saveUser) } }
        launch { viewModel.allUsers.collectLatest { users -> users.forEach(FirebaseCloudSync::saveUser) } }
        launch { viewModel.promotions.collectLatest { promos -> promos.forEach(FirebaseCloudSync::savePromotion) } }
        launch { viewModel.allTransactions.collectLatest { txs -> txs.forEach(FirebaseCloudSync::saveTransaction) } }
        launch { viewModel.notifications.collectLatest { notes -> notes.forEach(FirebaseCloudSync::saveNotification) } }
    }

    // BackHandler: handle hardware/gesture back press properly
    BackHandler(enabled = currentScreen != Screen.Home && currentScreen != Screen.Splash) {
        viewModel.navigateBack()
    }

    val showBottomBar = when (currentScreen) {
        Screen.Home, Screen.Earn, Screen.Wallet, Screen.CreatorProfile, Screen.MyContent -> true
        else -> false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                CreatorBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (currentScreen) {
                Screen.Splash -> SplashScreen(onFinished = { viewModel.finishSplash() })
                Screen.Onboarding -> OnboardingScreen(viewModel = viewModel)
                Screen.ChannelSetup -> ChannelSetupScreen(viewModel = viewModel)
                Screen.QuickVideoPromotion -> QuickVideoPromotionScreen(viewModel = viewModel)
                Screen.Login -> LoginScreen(viewModel = viewModel)
                Screen.Register -> RegisterScreen(viewModel = viewModel)
                Screen.ForgotPassword -> ForgotPasswordScreen(viewModel = viewModel)
                Screen.Home -> HomeScreen(viewModel = viewModel)
                Screen.Earn -> EarnScreen(viewModel = viewModel)
                Screen.TaskDetails -> TaskDetailsScreen(viewModel = viewModel)
                Screen.ContentFeed -> ContentFeedScreen(viewModel = viewModel)
                Screen.ContentDetails -> ContentDetailsScreen(viewModel = viewModel)
                Screen.SelectPromotionType, Screen.CreatePromotion -> SelectPromotionTypeScreen(viewModel = viewModel)
                Screen.AddYouTubeVideo -> AddYouTubeVideoScreen(viewModel = viewModel)
                Screen.AddYouTubeChannel -> AddYouTubeChannelScreen(viewModel = viewModel)
                Screen.PromotionBudget -> PromotionBudgetScreen(viewModel = viewModel)
                Screen.PromotionConfirmation -> PromotionConfirmationScreen(viewModel = viewModel)
                Screen.PromotionSuccess -> PromotionSuccessScreen(viewModel = viewModel)
                Screen.MyContent -> MyContentScreen(viewModel = viewModel)
                Screen.CreatorProfile -> CreatorProfileScreen(viewModel = viewModel)
                Screen.Wallet -> WalletScreen(viewModel = viewModel)
                Screen.TransactionHistory -> TransactionHistoryScreen(viewModel = viewModel)
                Screen.Notifications -> NotificationsScreen(viewModel = viewModel)
                Screen.Settings -> SettingsScreen(viewModel = viewModel)
                Screen.EditProfile -> EditProfileScreen(viewModel = viewModel)
                Screen.ReportContent -> ReportContentScreen(viewModel = viewModel)
                Screen.HelpSupport -> HelpSupportScreen(viewModel = viewModel)
                Screen.Terms -> TermsScreen(viewModel = viewModel)
                Screen.PrivacyPolicy -> PrivacyPolicyScreen(viewModel = viewModel)
                Screen.AdminPanel -> AdminPanelScreen(viewModel = viewModel)
            }
        }
    }
}
