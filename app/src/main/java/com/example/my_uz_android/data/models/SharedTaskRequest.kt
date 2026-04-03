package com.example.my_uz_android.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SharedTaskRequest(
    @SerialName("share_id") val shareId: String,
    @SerialName("tasks_json") val payload: String
)