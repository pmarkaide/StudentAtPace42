package com.pace42.student.models


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Student(
    val id: Int,
    val login: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("second_name")
    val secondName: String,
    val email: String,
    @SerialName("url")
    val profileUrl: String,
    @SerialName("pool_month")
    val poolMonth: String,
    @SerialName("pool_year")
    val poolYear: String,
    val cohort: String
)
