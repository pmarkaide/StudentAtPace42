package com.pace42.student

import com.pace42.student.plugins.configureHTTP
import com.pace42.student.routes.configureRouting
import com.pace42.student.plugins.configureSecurity
import com.pace42.student.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureHTTP()
    configureRouting()
}
