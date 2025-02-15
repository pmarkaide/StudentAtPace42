package com.pace42.student.quest

import com.pace42.student.student.Student
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class QuestName(
    val name: String,
)

@Serializable
data class Quest(
    @SerialName("validated_at")
    val validatedAt: String?,
    val quest: QuestName,
    val user: Student
)

@Serializable
data class QuestProgress(
    val cohort: String,
    val login: String,
    val rankName: String,
    val validatedDate: String?,
    val daysBehind: Long?
)