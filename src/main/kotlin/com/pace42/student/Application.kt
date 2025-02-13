package com.pace42.student

import com.pace42.student.auth.fetch42token

suspend fun main() {
    val token42 = fetch42token()
    println(token42)
}

