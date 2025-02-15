package com.pace42.student.quest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestName(
    val name: String,
)

@Serializable
data class QuestList(
    @SerialName("validated_at")
    val validatedAt: String,
    val quest: QuestName
)