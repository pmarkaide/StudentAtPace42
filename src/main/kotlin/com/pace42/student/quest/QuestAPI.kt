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
            val quests = client.get("https://api.intra.42.fr/v2/users/${login}/quests_users") {
                headers {
                    append("Authorization", "Bearer $token42")
                }
            }.body<List<Quest>>()

            return quests.map { quest ->
                quest.copy(
                    validatedAt = quest.validatedAt?.split("T")?.get(0)
                )
            }
        } catch (e: Exception) {
            println(e)
            return emptyList()
        }
    }

    suspend fun fetchQuestProgress(login: String): List<QuestProgress> {
        val quests = fetchUserQuests(login)
        if (quests.isEmpty()) return emptyList()

        val first = quests.first()
        val cohort = CohortUtils.getCohortFromYearMonth(first.user.poolYear, first.user.poolMonth)

        val startDate = when (cohort) {
            "Hiver5" -> "2023-10-23"
            "Hiver6" -> "2024-04-15"
            "Hiver7" -> "2024-10-28"
            else -> "Unknown"
        }

        val rankDeadlines = listOf(
            "Common Core Rank 00" to 28,
            "Common Core Rank 01" to 90,
            "Common Core Rank 02" to 181,
            "Common Core Rank 03" to 261,
            "Common Core Rank 04" to 361,
            "Common Core Rank 05" to 480,
            "Common Core Rank 06" to 550
        )

        val today = TimeUtils.getToday()
        val daysSinceStart = TimeUtils.daysBetween(startDate, today) ?: 0

        // Create a map of completed quests for lookup
        val completedQuests = quests.associate { it.quest.name to it.validatedAt }

        // Find the first uncompleted rank
        val firstUncompletedRank = rankDeadlines.firstOrNull { (rankName, _) ->
            !completedQuests.containsKey(rankName)
        }?.first

        // Generate progress for all ranks
        return rankDeadlines.map { (rankName, deadline) ->
            val validatedAt = completedQuests[rankName]

            val dayDifference = when {
                // For completed ranks: compare validation date against deadline
                validatedAt != null -> {
                    val daysToComplete = TimeUtils.daysBetween(startDate, validatedAt) ?: 0
                    deadline - daysToComplete // Negative means late completion
                }
                // For the first uncompleted rank: compare current days against deadline
                rankName == firstUncompletedRank -> {
                    deadline - daysSinceStart
                }
                else -> null // mark rest as null
            }

            QuestProgress(
                cohort = cohort,
                login = first.user.login,
                rankName = rankName,
                validatedDate = validatedAt,
                daysBuffer = dayDifference
            )
        }
    }
}