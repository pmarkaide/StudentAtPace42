package com.pace42.student.utils

object CohortUtils {
    data class CohortDates(
        val year: String,
        val month: String,
    )

    fun getYearMonthFromCohort(cohort: String): CohortDates {
        return when (cohort) {
            "Hiver5" -> CohortDates("2023", "july,august")
            "Hiver6" -> CohortDates("2024", "january,february")
            "Hiver7" -> CohortDates("2024", "july,august,september")
            else -> CohortDates("Unknown", "Unknown")
        }
    }

    fun getCohortFromYearMonth(year: String, month: String): String {
        return when {
            year == "2023" && month.lowercase() in listOf("july", "august") -> "Hiver5"
            year == "2024" && month.lowercase() in listOf("january", "february") -> "Hiver6"
            year == "2024" && month.lowercase() in listOf("july", "august", "september") -> "Hiver7"
            else -> "Unknown"
        }
    }
}