package com.example.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth

object FirebaseGoogleAuth {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signInIntent(activity: Activity): Intent {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.example.R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, options).signInIntent
    }

    fun completeSignIn(data: Intent?, nameOverride: String? = null, onResult: (Result<FirebaseUserData>) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(Exception::class.java)
            val idToken = account.idToken ?: throw IllegalStateException("Google ID token missing")
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener {
                    val user = auth.currentUser ?: throw IllegalStateException("Firebase user missing")
                    onResult(Result.success(FirebaseUserData(user.uid, user.displayName.orEmpty(), user.email.orEmpty(), user.photoUrl?.toString().orEmpty())))
                }
                .addOnFailureListener { onResult(Result.failure(it)) }
        } catch (e: Exception) {
            onResult(Result.failure(e))
        }
    }

    fun currentUser() = auth.currentUser
}

data class FirebaseUserData(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String
)
