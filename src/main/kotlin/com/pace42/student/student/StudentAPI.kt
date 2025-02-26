package com.pace42.student.student

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import com.pace42.student.utils.CohortUtils


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


    // Coroutine fetch of all cohorts
    suspend fun fetchCohorts(vararg cohorts: String): List<Student> = coroutineScope {
        cohorts.map { cohort ->
            async { fetchCohort(cohort) }
        }.awaitAll().flatten()
    }

    private suspend fun fetchCohort(cohort: String): List<Student> {
        // set pagination
        var currentPage = 1
        val pageSize = 100
        val allStudents = mutableListOf<Student>()

        val cohortDates = CohortUtils.getYearMonthFromCohort(cohort)
        try {
            while (true) {
                delay(500)
                val baseUrl = "https://api.intra.42.fr/v2/campus/13"
                val response =
                    client.get("$baseUrl/users?filter[pool_year]=${cohortDates.year}&filter[pool_month]=${cohortDates.month}") {
                        parameter("page[number]", currentPage)
                        parameter("page[size]", pageSize)
                        headers {
                            append("Authorization", "Bearer $token42")
                        }
                    }

                // Handle rate limiting
                when (response.status.value) {
                    429 -> {
                        println("Rate limit hit, waiting before retry...")
                        delay(5000) // Wait 10 seconds before retrying
                        continue
                    }
                    200 -> {
                        val pageStudents = response.body<List<Student>>()
                        allStudents.addAll(pageStudents)

                        if (pageStudents.size < pageSize) {
                            break
                        }
                        currentPage++
                    }
                    else -> {
                        println("Unexpected response: ${response.status}")
                        break
                    }
                }
            }

            // add cohort tag to every student and correct urls
            return allStudents
                .filter { it.active }  // Only keep active students
                .map { student ->
                    student.copy(
                        cohort = cohort,
                        profileUrl = student.profileUrl + student.login,
                        graphUrl = student.graphUrl + student.login
                    )
                }

        } catch (e: Exception) {
            println(e)
            return emptyList()
        }
    }

    suspend fun fetchStudent(login: String): List<Student> {
        val response = client.get("https://api.intra.42.fr/v2/users?"){
            headers {
                append("Authorization", "Bearer $token42")
            }
            parameter("filter[login]", login)
        }

        return response.body<List<Student>>().map { student ->
            val cohort = CohortUtils.getCohortFromYearMonth(student.poolYear, student.poolMonth)
            student.copy(
                cohort = cohort,
                profileUrl = student.profileUrl + student.login,
                graphUrl = student.graphUrl + student.login
            )
        }

    }
}

