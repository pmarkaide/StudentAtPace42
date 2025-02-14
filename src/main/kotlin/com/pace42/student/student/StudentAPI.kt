package com.pace42.student.student

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

// https://api.intra.42.fr/v2/campus/13/users?filter[pool_year]=2023&filter[pool_month]=july,august

class StudentAPI(private val token42: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    fun close() {
        client.close()
    }

    data class CohortDates(
        val year: String,
        val month: String,
    )

    private fun getYearMonthFromCohort(cohort: String): CohortDates {
        return when (cohort) {
            "Hiver5" -> CohortDates("2023", "july,august")
            "Hiver6" -> CohortDates("2024", "january,february")
            "Hiver7" -> CohortDates("2024", "july,august,september")
            else -> CohortDates("Unknown", "Unknown")
        }
    }

    // Coroutine fetch of all cohorts
    suspend fun fetchCohorts(vararg cohorts: String): List<Student> = coroutineScope {
        cohorts.map { cohort ->
            async { fetchCohort(cohort) }
        }.awaitAll().flatten()
    }

    suspend fun fetchCohort(cohort: String): List<Student> {
        // set pagination
        var currentPage = 1
        val pageSize = 30
        val allStudents = mutableListOf<Student>()

        val cohortDates = getYearMonthFromCohort(cohort)
        try {
            while (true) {
                val baseUrl = "https://api.intra.42.fr/v2/campus/13"
                val response =
                    client.get("$baseUrl/users?filter[pool_year]=${cohortDates.year}&filter[pool_month]=${cohortDates.month}") {
                        parameter("page[number]", currentPage)
                        parameter("page[size]", pageSize)
                        headers {
                            append("Authorization", "Bearer $token42")
                        }
                    }

                val pageStudents = response.body<List<Student>>()
                allStudents.addAll(pageStudents)

                if (pageStudents.size < pageSize) {
                    break
                }
                currentPage++
            }

            // add cohort tag to every student
            return allStudents.map { student ->
                student.copy(cohort = cohort)
            }

        } catch (e: Exception) {
            println(e)
            return emptyList()
        }
    }
}

