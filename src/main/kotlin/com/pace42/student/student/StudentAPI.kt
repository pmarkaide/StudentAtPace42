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

    suspend fun fetchCohort(year: String, month: String): List<Student> {

        val baseUrl = "https://api.intra.42.fr/v2/campus/13"
        val response = client.get("$baseUrl/users?filter[pool_year]=$year&filter[pool_month]=$month") {
            headers {
                append("Authorization", "Bearer $token42")
            }
        }
        val rawBody = response.bodyAsText()
        println("Raw API Response:")
        println(rawBody)

        return response.body<List<Student>>()
    }
}
