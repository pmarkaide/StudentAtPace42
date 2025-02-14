package com.pace42.student

import com.pace42.student.auth.fetch42token
import com.pace42.student.student.StudentAPI

suspend fun main() {
    val token42 = try {
        fetch42token()
    } catch (e: Exception) {
        return
    }

    val studentAPI = StudentAPI(token42)
    try {
        val  students = studentAPI.fetchCohorts("Hiver5", "Hiver6", "Hiver7")
        studentAPI.close()
        println("Number of students: ${students.size}")
        if (students.isNotEmpty()) {
            println("First student: ${students.first()}")
        } else {
            println("No students found")
        }

    } catch (e: Exception) {
        return
    }
}

