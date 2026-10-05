package com.example.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object FirebaseGoogleAuth {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signInIntent(activity: Activity): Intent {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.example.R.string.default_web_client_id))
            .requestEmail().build()
        return GoogleSignIn.getClient(activity, options).signInIntent
    }

    fun completeSignIn(data: Intent?, nameOverride: String? = null, onResult: (Result<FirebaseUserData>) -> Unit) {
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(Exception::class.java)
            val idToken = account.idToken ?: throw IllegalStateException("Google ID token missing")
            auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener {
                    val user = auth.currentUser ?: return@addOnSuccessListener onResult(Result.failure(IllegalStateException("Firebase user missing")))
                    val name = nameOverride?.trim().takeUnless { it.isNullOrBlank() } ?: user.displayName.orEmpty()
                    onResult(Result.success(FirebaseUserData(user.uid,name,user.email.orEmpty(),user.photoUrl?.toString().orEmpty())))
                }.addOnFailureListener { onResult(Result.failure(it)) }
        } catch(e:Exception){ onResult(Result.failure(e)) }
    }

    fun signInWithEmail(email:String,password:String,onResult:(Result<FirebaseUserData>)->Unit){
        auth.signInWithEmailAndPassword(email.trim(),password).addOnSuccessListener {
            val u=auth.currentUser
            if(u==null) onResult(Result.failure(IllegalStateException("Firebase user missing")))
            else onResult(Result.success(FirebaseUserData(u.uid,u.displayName.orEmpty(),u.email.orEmpty(),u.photoUrl?.toString().orEmpty())))
        }.addOnFailureListener{onResult(Result.failure(it))}
    }

    fun createAccountWithEmail(name:String,email:String,password:String,onResult:(Result<FirebaseUserData>)->Unit){
        auth.createUserWithEmailAndPassword(email.trim(),password).addOnSuccessListener {
            val u=auth.currentUser
            if(u==null){onResult(Result.failure(IllegalStateException("Firebase user missing")));return@addOnSuccessListener}
            val clean=name.trim()
            u.updateProfile(com.google.firebase.auth.userProfileChangeRequest{displayName=clean})
                .addOnCompleteListener{onResult(Result.success(FirebaseUserData(u.uid,clean,u.email.orEmpty(),u.photoUrl?.toString().orEmpty())))}
        }.addOnFailureListener{onResult(Result.failure(it))}
    }

    fun sendPasswordReset(email:String,onResult:(Result<Unit>)->Unit){
        auth.sendPasswordResetEmail(email.trim()).addOnSuccessListener{onResult(Result.success(Unit))}
            .addOnFailureListener{onResult(Result.failure(it))}
    }

    fun currentUser()=auth.currentUser
}

data class FirebaseUserData(val uid:String,val name:String,val email:String,val photoUrl:String)
