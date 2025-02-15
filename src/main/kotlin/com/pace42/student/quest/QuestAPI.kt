package com.pace42.student.quest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json



class QuestAPI(private val token42: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                coerceInputValues = true
            })
        }
    }

    fun close() {
        client.close()
    }

//    // Coroutine fetch of all cohorts
//    suspend fun fetchCohorts(vararg cohorts: String): List<Student> = coroutineScope {
//        cohorts.map { cohort ->
//            async { fetchCohort(cohort) }
//        }.awaitAll().flatten()
//    }

    suspend fun fetchQuestList(login: String): List<Quest> {

        try {
             return  client.get("https://api.intra.42.fr/v2/users/${login}/quests_users") {
                     headers {
                        append("Authorization", "Bearer $token42")
                    }
                }.body<List<Quest>>()
        } catch (e: Exception) {
            println(e)
            return emptyList()
        }
    }
}