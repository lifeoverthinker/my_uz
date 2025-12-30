package com.example.my_uz_android.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SharedTaskRequest(
    @SerialName("share_id") val shareId: String,
    // Mapujemy pole 'payload' w kodzie na kolumnę 'tasks_json' w bazie danych
    @SerialName("tasks_json") val payload: String
)