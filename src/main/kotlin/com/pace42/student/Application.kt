package com.pace42.student

import com.pace42.student.auth.fetch42token
import com.pace42.student.export.StudentCSVExporter
import com.pace42.student.quest.QuestAPI
import com.pace42.student.quest.QuestProgress
import com.pace42.student.student.Student
import com.pace42.student.student.StudentAPI
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

suspend fun main() {
    val token42 = try {
        fetch42token()
    } catch (e: Exception) {
        return
    }

    try {
//        val  students = studentAPI.fetchCohorts("Hiver5", "Hiver6", "Hiver7")
//        studentAPI.close()
//        println("Number of students: ${students.size}")

//        // Export to CSV
//        val outputPath = Path("students_export.csv")
//        StudentCSVExporter.exportBasicStudentInfo(students, outputPath)
//        println("CSV exported successfully to: ${outputPath.absolutePathString()}")
//        val quests = questAPI.fetchQuestProgress("upolat")
//        quests.forEach { quest ->
//            println(quest)
//        }


        val questAPI = QuestAPI(token42)
        try {
            val quests = questAPI.fetchCampusQuests()
            println("Fetched quests for ${quests.size} quest entries")
        } finally {
            questAPI.close()
        }

    } catch (e: Exception) {
        return
    }
}

