package com.pace42.student

import com.pace42.student.auth.fetch42token
import com.pace42.student.export.*
import com.pace42.student.quest.QuestAPI
import com.pace42.student.quest.QuestProgress
import com.pace42.student.student.Student
import com.pace42.student.student.StudentAPI
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlinx.coroutines.runBlocking

fun main() {
    println("Starting student data export application...")

    // Run the application in a blocking context
    runBlocking {
        println("Fetching 42 API token...")
        val token42 = try {
            fetch42token()
        } catch (e: Exception) {
            System.err.println("Error fetching token: ${e.message}")
            e.printStackTrace()
            return@runBlocking
        }

        println("Token fetched successfully!")

        val questAPI = QuestAPI(token42)
        val studentAPI = StudentAPI(token42)

        try {
            println("Fetching student cohorts...")
            val students = studentAPI.fetchCohorts("Hiver5", "Hiver6", "Hiver7")
            println("Fetched ${students.size} active students")

            println("Fetching campus quests...")
            val quests = questAPI.fetchCampusQuests()
            println("Fetched ${quests.size} quests")

            println("Calculating quest progress...")
            val progress = questAPI.calculateCampusQuestProgress(quests)
            println("Calculated progress for ${progress.size} quest entries")

            // Update the output path to use the volume mount point
            val outputPath = Path("/app/output/student_progress.csv")
            println("Exporting data to ${outputPath.absolutePathString()}")

            StudentProgressCSVExporter.exportStudentsProgress(
                students = students,
                questProgress = progress,
                outputPath = outputPath
            )
            println("CSV exported successfully to ${outputPath.absolutePathString()}")
        } catch (e: Exception) {
            System.err.println("Error during execution: ${e.message}")
            e.printStackTrace()
        } finally {
            println("Closing API connections...")
            studentAPI.close()
            questAPI.close()
            println("Application execution completed")
        }
    }
}