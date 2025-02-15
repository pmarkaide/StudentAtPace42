package com.pace42.student.export

import com.pace42.student.student.Student
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.absolutePathString

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
