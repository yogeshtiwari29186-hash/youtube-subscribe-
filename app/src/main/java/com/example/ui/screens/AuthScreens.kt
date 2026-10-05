package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.auth.FirebaseGoogleAuth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.ComplianceNoticeCard
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CreatorDiamondViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1.1f, animationSpec = tween(600))
        scale.animateTo(1.0f, animationSpec = tween(300))
        delay(1000)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(DiamondCyan, ElectricBlue)))
                    .border(2.dp, Color(0xFF99F4FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Diamond,
                    contentDescription = "CreatorDiamond Logo",
                    tint = Color(0xFF0A0D14),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CreatorDiamond",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Discover · Promote · Earn",
                style = MaterialTheme.typography.bodyMedium,
                color = DiamondCyan,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    viewModel: CreatorDiamondViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DiamondCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = R.drawable.creator_hero_banner_1791198233578,
                    contentDescription = "Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to CreatorDiamond",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A two-sided creator platform where every account is both a Viewer and a Creator. Earn diamonds through in-app activities and spend them to amplify your YouTube channel.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            ComplianceNoticeCard()
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_get_started_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DiamondCyan,
                    contentColor = Color(0xFF0A0D14)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Started 🚀", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.Home) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Text("Skip to Community Feed", color = TextPrimary)
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: CreatorDiamondViewModel, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    val loading by viewModel.authLoading.collectAsState()
    Column(modifier.fillMaxSize().background(DarkBackground).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Spacer(Modifier.height(60.dp)); Icon(Icons.Filled.Security,null,tint=DiamondCyan,modifier=Modifier.size(56.dp))
        Spacer(Modifier.height(20.dp)); Text("Login",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold,color=TextPrimary)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(email,{email=it},label={Text("Email")},leadingIcon={Icon(Icons.Filled.Email,null)},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password,{password=it},label={Text("Password")},leadingIcon={Icon(Icons.Filled.Lock,null)},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(18.dp))
        Button({if(!loading&&email.isNotBlank()&&password.length>=6)viewModel.loginWithEmail(email,password)},Modifier.fillMaxWidth().height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=DiamondCyan,contentColor=Color(0xFF0A0D14))){Text(if(loading)"Logging in..." else "Login",fontWeight=FontWeight.Bold)}
        Spacer(Modifier.height(8.dp)); OutlinedButton({viewModel.navigateTo(Screen.ForgotPassword)},Modifier.fillMaxWidth()){Text("Forgot Password")}
        Spacer(Modifier.height(8.dp)); OutlinedButton({viewModel.navigateTo(Screen.Register)},Modifier.fillMaxWidth(),border=BorderStroke(1.dp,DiamondCyan)){Text("Create New Account",color=DiamondCyan)}
    }
}

@Composable
fun RegisterScreen(viewModel: CreatorDiamondViewModel, modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var confirm by remember { mutableStateOf("") }
    val loading by viewModel.authLoading.collectAsState()
    Column(modifier.fillMaxSize().background(DarkBackground).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Spacer(Modifier.height(32.dp)); IconButton({viewModel.navigateBack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back",tint=TextPrimary)}
        Text("Create Your Account",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold,color=TextPrimary); Spacer(Modifier.height(16.dp))
        OutlinedTextField(name,{name=it},label={Text("Full Name")},leadingIcon={Icon(Icons.Filled.Person,null)},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(10.dp)); OutlinedTextField(email,{email=it},label={Text("Email")},leadingIcon={Icon(Icons.Filled.Email,null)},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(10.dp)); OutlinedTextField(password,{password=it},label={Text("Password (6+ characters)")},leadingIcon={Icon(Icons.Filled.Lock,null)},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(10.dp)); OutlinedTextField(confirm,{confirm=it},label={Text("Confirm Password")},leadingIcon={Icon(Icons.Filled.Lock,null)},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp)); Button({if(!loading&&name.isNotBlank()&&email.isNotBlank()&&password.length>=6&&password==confirm)viewModel.registerWithEmail(name,email,password)},Modifier.fillMaxWidth().height(54.dp),colors=ButtonDefaults.buttonColors(containerColor=DiamondCyan,contentColor=Color(0xFF0A0D14))){Text(if(loading)"Creating..." else "Create Account",fontWeight=FontWeight.Bold)}
        Spacer(Modifier.height(10.dp)); Text("50 💎 welcome Diamonds for a new Firebase account.",color=DiamondGold,fontSize=12.sp)
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: CreatorDiamondViewModel, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }; val loading by viewModel.authLoading.collectAsState()
    Column(modifier.fillMaxSize().background(DarkBackground).padding(24.dp)){
        IconButton({viewModel.navigateBack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back",tint=TextPrimary)}
        Text("Reset Password",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold,color=TextPrimary)
        Spacer(Modifier.height(20.dp)); OutlinedTextField(email,{email=it},label={Text("Email Address")},leadingIcon={Icon(Icons.Filled.Email,null)},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(20.dp)); Button({if(!loading&&email.isNotBlank())viewModel.resetPassword(email)},Modifier.fillMaxWidth().height(50.dp),colors=ButtonDefaults.buttonColors(containerColor=DiamondCyan,contentColor=Color(0xFF0A0D14))){Text(if(loading)"Sending..." else "Send Reset Link")}
    }
}
