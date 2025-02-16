package com.pace42.student.student


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Student(
    val id: Int,
    val login: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    @SerialName("pool_month") val poolMonth: String,
    @SerialName("pool_year") val poolYear: String,
    val profileUrl: String = "https://profile.intra.42.fr/users/",
    val graphUrl: String = "https://projects.intra.42.fr/projects/graph?login=",
    val cohort: String = "Unknown",
    @SerialName("active?")val active: Boolean = true,
)
