package com.pace42.student.export

import com.pace42.student.student.Student
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import com.pace42.student.quest.QuestProgress

class StudentCSVExporter {
    companion object {
        // Headers for different export types
        private val STUDENT_BASIC_HEADERS = arrayOf(
            "Cohort",
            "Login",
            "First Name",
            "Last Name",
            "Email",
            "Pool Month",
            "Pool Year",
            "Profile URL",
            "Graph URL"
        )

        fun exportBasicStudentInfo(students: List<Student>, outputPath: Path) {
            exportToCSV(students, outputPath, STUDENT_BASIC_HEADERS) { student ->
                arrayOf(
                    student.cohort,
                    student.login,
                    student.firstName,
                    student.lastName,
                    student.email,
                    student.poolMonth,
                    student.poolYear,
                    student.profileUrl,
                    student.graphUrl
                )
            }
        }

         private fun exportToCSV(
            students: List<Student>,
            outputPath: Path,
            headers: Array<String>,
            recordMapper: (Student) -> Array<String>
        ) {
            FileWriter(outputPath.absolutePathString()).use { writer ->
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader(*headers)
                    .build()
                    .print(writer)
                    .use { csvPrinter ->
                        students.forEach { student ->
                            csvPrinter.printRecord(*recordMapper(student))
                        }
                    }
            }
        }
    }
}

class StudentProgressCSVExporter {
    companion object {
        private val BASE_HEADERS = arrayOf(
            "Cohort",
            "Login",
            "First Name",
            "Last Name",
            "Active Rank Buffer",
            "Email",
            "Pool Month",
            "Pool Year",
            "Profile URL",
            "Graph URL"
        )

        private val RANK_NAMES = arrayOf(
            "Common Core Rank 00",
            "Common Core Rank 01",
            "Common Core Rank 02",
            "Common Core Rank 03",
            "Common Core Rank 04",
            "Common Core Rank 05",
            "Common Core Rank 06"
        )

        private fun findLastCompletedRankBuffer(rankBuffers: Map<String, String>): String {
            val lastRankName = RANK_NAMES.findLast { rankName ->
                rankBuffers[rankName]?.isNotEmpty() == true
            }
            return lastRankName?.let { rankBuffers[it] } ?: "Not Started"
        }

        fun exportStudentsProgress(
            students: List<Student>,
            questProgress: List<QuestProgress>,
            outputPath: Path
        ) {
            // Create a map of login to quest progress records
            val progressByLogin = questProgress.groupBy { it.login }

            // Combine BASE_HEADERS with RANK_NAMES for complete header list
            val allHeaders = BASE_HEADERS + RANK_NAMES

            FileWriter(outputPath.absolutePathString()).use { writer ->
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader(*allHeaders)
                    .build()
                    .print(writer)
                    .use { csvPrinter ->
                        students.forEach { student ->
                            // Get progress records for this student
                            val studentProgress = progressByLogin[student.login] ?: emptyList()

                            // Create a map of rank name to days buffer for easier lookup
                            val rankBuffers = studentProgress.associate {
                                it.rankName to (it.daysBuffer?.toString() ?: "")
                            }

                            // Find the last completed rank
                            val activeRankBuffer = findLastCompletedRankBuffer(rankBuffers)

                            // Build the complete record with base info and rank buffers
                            val record = mutableListOf(
                                student.cohort,
                                student.login,
                                student.firstName,
                                student.lastName,
                                student.email,
                                activeRankBuffer,
                                student.poolMonth,
                                student.poolYear,
                                student.profileUrl,
                                student.graphUrl
                            )

                            // Add the rank buffer values in order
                            RANK_NAMES.forEach { rankName ->
                                record.add(rankBuffers[rankName] ?: "")
                            }

                            csvPrinter.printRecord(record)
                        }
                    }
            }
        }
    }
}
