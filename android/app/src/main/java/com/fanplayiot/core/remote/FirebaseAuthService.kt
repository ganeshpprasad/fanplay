package com.fanplayiot.core.remote

import com.fanplayiot.core.db.local.repository.HomeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseAuthService {
    fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
    }

    @JvmStatic
    fun getTokenId(firebaseAuth: FirebaseAuth, repository: HomeRepository) {
        val currUser = firebaseAuth.currentUser ?: return
        currUser.getIdToken(true)
                .addOnSuccessListener { getTokenResult ->
                    getTokenResult?.run {
                        token?.let { tokenId -> repository.updateTokenId(tokenId, expirationTimestamp) }
                    }

                }.addOnFailureListener {
                    logoutUser()
                }
    }

    @JvmStatic
    fun currentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}