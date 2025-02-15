package com.pace42.student.quest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.pace42.student.utils.CohortUtils
import com.pace42.student.utils.TimeUtils


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

    suspend fun fetchUserQuests(login: String): List<Quest> {

        try {
             val quests =  client.get("https://api.intra.42.fr/v2/users/${login}/quests_users") {
                     headers {
                        append("Authorization", "Bearer $token42")
                    }
                }.body<List<Quest>>()

            return quests.map { quest ->
                quest.copy(
                    validatedAt = quest.validatedAt?.split("T")?.get(0)
                ) }
        } catch (e: Exception) {
            println(e)
            return emptyList()
        }
    }

    suspend fun fetchQuestProgress(login: String): List<QuestProgress> {
        val quests = fetchUserQuests(login)

        val first = quests.first()
        val cohort = CohortUtils.getCohortFromYearMonth(first.user.poolYear, first.user.poolMonth)

        val startDate = when(cohort) {
            "Hiver5" -> "2023-10-23"
            "Hiver6" -> "2024-04-15"
            "Hiver7" -> "2024-10-28"
            else -> "Unknown"
        }
        println("cohort: $cohort startDate: $startDate")

        fun getHardDeadline(rank: String): Int? {
            return when (rank) {
                "Common Core Rank 00" -> 28
                "Common Core Rank 01" -> 90
                "Common Core Rank 02" -> 181
                "Common Core Rank 03" -> 261
                "Common Core Rank 04" -> 361
                "Common Core Rank 05" -> 480
                "Common Core Rank 06" -> 550
                else -> null
            }
        }
        return quests.map { quest ->
            val daysNeededToEvaluate = TimeUtils.daysBetween(startDate, quest.validatedAt)
            val hardDeadline = getHardDeadline(quest.quest.name)
            val dayDifference = if (hardDeadline != null && daysNeededToEvaluate != null) {
                hardDeadline - daysNeededToEvaluate
            } else {
                null
            }
            QuestProgress(cohort, first.user.login, quest.quest.name, quest.validatedAt, dayDifference)
        }
    }
}