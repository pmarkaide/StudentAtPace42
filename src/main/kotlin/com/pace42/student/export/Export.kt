package com.pace42.student.export

import com.pace42.student.student.Student
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class StudentCSVExporter {
    companion object {
        private val HEADERS = arrayOf(
            "ID",
            "Login",
            "First Name",
            "Last Name",
            "Email",
            "Profile URL",
            "Pool Month",
            "Pool Year",
            "Cohort"
        )

        fun exportToCSV(students: List<Student>, outputPath: Path) {
            FileWriter(outputPath.absolutePathString()).use { writer ->
                CSVFormat.DEFAULT
                    .builder()
                    .setHeader(*HEADERS)
                    .build()
                    .print(writer)
                    .use { csvPrinter ->
                        students.forEach { student ->
                            csvPrinter.printRecord(
                                student.id,
                                student.login,
                                student.firstName,
                                student.lastName,
                                student.email,
                                student.profileUrl,
                                student.poolMonth,
                                student.poolYear,
                                student.cohort
                            )
                        }
                    }
            }
        }
    }
}