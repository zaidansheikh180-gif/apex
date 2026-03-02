package com.apex.coach.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Shared API Client (PRD 2.3)
 * Handles Auth (JWT) and API requests for both iOS and Android.
 */
class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        
        install(Auth) {
            bearer {
                loadTokens {
                    // Load JWT and Refresh tokens (PRD 2.1)
                    BearerTokens("access_token", "refresh_token")
                }
                refreshTokens {
                    // Logic to refresh JWT
                    BearerTokens("new_access_token", "new_refresh_token")
                }
            }
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 3000 // PRD 2.5: Timeout > 3s
        }
    }

    suspend fun getScore(): Int {
        // GET /v1/score
        return 78 // Mocked for now
    }
}
