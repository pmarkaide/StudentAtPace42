package com.pace42.student

import com.pace42.student.auth.fetch42token
import com.pace42.student.export.*
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

    val questAPI = QuestAPI(token42)
    val studentAPI = StudentAPI(token42)

    try {
        val students = studentAPI.fetchCohorts("Hiver5", "Hiver6", "Hiver7")
        val quests = questAPI.fetchCampusQuests()
        val progress = questAPI.calculateCampusQuestProgress(quests)

        StudentProgressCSVExporter.exportStudentsProgress(
            students = students,
            questProgress = progress,
            outputPath = Path("student_progress.csv")
        )
    } catch (e: Exception) {
        println(e)
        e.printStackTrace()
    } finally {
        studentAPI.close()
        questAPI.close()
    }
}

