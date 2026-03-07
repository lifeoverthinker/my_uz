package com.example.my_uz_android.data

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Wywoła się, gdy telefon dostanie powiadomienie
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            Log.d("FCM", "Dostałam wiadomość: ${it.body}")
            // Tu możesz dodać kod do pokazania powiadomienia na pasku
        }
    }

    // Wywoła się raz przy instalacji - to Twój unikalny ID
    override fun onNewToken(token: String) {
        Log.d("FCM", "Mój token to: $token")
        // Docelowo wyślesz ten token do Supabase, by baza wiedziała komu wysłać pusha
    }
}