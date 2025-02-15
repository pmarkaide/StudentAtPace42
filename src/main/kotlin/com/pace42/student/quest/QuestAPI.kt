package com.pace42.student.quest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import kotlinx.coroutines.delay

import com.pace42.student.utils.CohortUtils
import com.pace42.student.utils.TimeUtils
import com.pace42.student.student.StudentAPI
import com.pace42.student.student.Student
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


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

    private fun isValidStudent(student: Student): Boolean {
        return !student.login.isNullOrEmpty() &&
                !student.firstName.isNullOrEmpty() &&
                !student.lastName.isNullOrEmpty() &&
                !student.email.isNullOrEmpty() &&
                !student.poolMonth.isNullOrEmpty() &&
                !student.poolYear.isNullOrEmpty()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        coerceInputValues = true
    }

    private suspend inline fun <reified T> safeJsonParse(jsonElement: JsonElement): T? {
        return try {
            json.decodeFromJsonElement<T>(jsonElement)
        } catch (e: Exception) {
            println("Failed to parse JSON: ${e.message}")
            println("Problematic JSON: $jsonElement")
            null
        }
    }

    suspend fun fetchCampusQuests(): List<Quest> {
        var currentPage = 1
        val pageSize = 100
        val allQuests = mutableListOf<Quest>()

        try {
            while (true) {
                val response = client.get("https://api.intra.42.fr/v2/quests_users?filter[campus_id]=13") {
                    parameter("page[number]", currentPage)
                    parameter("page[size]", pageSize)
                    headers {
                        append("Authorization", "Bearer $token42")
                    }
                }

                when (response.status.value) {
                    429 -> {
                        println("Rate limit hit, waiting before retry...")
                        delay(5000)
                        continue
                    }
                    200 -> {
                        // Parse response as JsonArray first
                        val jsonArray = response.body<JsonArray>()

                        // Filter out quests with invalid student data during parsing
                        val validQuests = jsonArray.mapNotNull { jsonElement ->
                            val quest = safeJsonParse<Quest>(jsonElement)
                            if (quest == null) {
                                println("Failed to parse quest")
                                null
                            } else if (!isValidStudent(quest.user)) {
                                println("Invalid student data for user ${quest.user.login}: " +
                                        "firstName=${quest.user.firstName}, " +
                                        "lastName=${quest.user.lastName}, " +
                                        "email=${quest.user.email}, " +
                                        "poolMonth=${quest.user.poolMonth}, " +
                                        "poolYear=${quest.user.poolYear}")
                                null
                            } else {
                                quest.copy(
                                    validatedAt = quest.validatedAt?.split("T")?.get(0)
                                )
                            }
                        }

                        println("Successfully got ${validQuests.size} valid quests")
                        allQuests.addAll(validQuests)

                        if (validQuests.size < pageSize) {
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

            return allQuests

        } catch (e: Exception) {
            println("Error in fetchCampusQuests: ${e.message}")
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

    suspend fun fetchCohortsQuestProgress(vararg cohorts: String): List<QuestProgress> = coroutineScope {
        val studentAPI = StudentAPI(token42)
        val students = studentAPI.fetchCohorts(*cohorts)

        val allProgress = mutableListOf<QuestProgress>()

        val chunkSize = 2
        students.chunked(chunkSize).forEach { chunk ->
            // Process each chunk concurrently
            val chunkProgress = chunk.map { student ->
                async {
                    try {
                        delay(500)
                        println("fetching ${student.login} quests...")
                        fetchQuestProgress(student.login)
                    } catch (e: Exception) {
                        println("Error fetching progress for ${student.login}: ${e.message}")
                        emptyList()
                    }
                }
            }.awaitAll()

            // Add all progress from this chunk to our result list
            allProgress.addAll(chunkProgress.flatten())
        }

        allProgress
    }
}