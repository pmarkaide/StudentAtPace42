package com.pace42.student.auth

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.utils.io.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.serialization.kotlinx.json.*

@Serializable
data class Token(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("scope") val scope: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("secret_valid_until") val secretValidUntil: Long
)

@OptIn(InternalAPI::class)
suspend fun fetch42token(): String {

    val dotenv = dotenv()
    val userId42 = dotenv["UID_42"]
    val secret42 = dotenv["SECRET_42"]

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        val token: Token = client.post("https://api.intra.42.fr/oauth/token"){
            body = FormDataContent(Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", userId42)
                append("client_secret", secret42)
            })
        }.body()

        return token.accessToken

    } catch (e: Exception) {
        println(" Error fetching 42 token: ${e.message}")
        throw e
    }
}