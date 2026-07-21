package com.iti.careerpilot.reports.data

import com.iti.careerpilot.core.network.Endpoints
import com.iti.careerpilot.reports.data.datasource.remote.ReportsRemoteDataSourceImpl
import com.iti.common.result.CareerPilotResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportsRemoteDataSourceImplTest {
    @Test
    fun `sessions endpoint unwraps the backend response envelope`() = runTest {
        val dataSource = ReportsRemoteDataSourceImpl(
            client = jsonClient(
                expectedUrl = Endpoints.INTERVIEW_SESSIONS,
                responseBody = SESSIONS_RESPONSE,
            ),
        )

        val result = dataSource.getSessions()

        assertTrue(result is CareerPilotResult.Success)
        val sessions = (result as CareerPilotResult.Success).data
        assertEquals(1, sessions.size)
        assertEquals("Software Engineering", sessions.single().trackName)
    }

    @Test
    fun `question endpoint uses the requested session id`() = runTest {
        val dataSource = ReportsRemoteDataSourceImpl(
            client = jsonClient(
                expectedUrl = Endpoints.interviewSessionQuestions(42L),
                responseBody = QUESTIONS_RESPONSE,
            ),
        )

        val result = dataSource.getQuestions(42L)

        assertTrue(result is CareerPilotResult.Success)
        assertEquals(1, (result as CareerPilotResult.Success).data.single().questionOrder)
    }
}

private fun jsonClient(
    expectedUrl: String,
    responseBody: String,
): HttpClient = HttpClient(
    MockEngine { request ->
        assertEquals(expectedUrl, request.url.toString())
        respond(
            content = responseBody,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    },
) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

private const val SESSIONS_RESPONSE = """
    {
      "message": "Sessions retrieved",
      "success": true,
      "timestamp": "2026-07-19T11:14:01",
      "data": [{
        "id": 1,
        "trackId": 4,
        "trackName": "Software Engineering",
        "status": "COMPLETED",
        "overallScore": 82,
        "durationSeconds": 102,
        "maxQuestions": 3,
        "completedAt": "2026-07-19T11:14:00",
        "createdAt": "2026-07-19T11:12:18"
      }]
    }
"""

private const val QUESTIONS_RESPONSE = """
    {
      "message": "Questions retrieved",
      "success": true,
      "timestamp": "2026-07-19T11:14:01",
      "data": [{
        "id": 11,
        "sessionId": 42,
        "questionText": "Tell me about yourself.",
        "questionOrder": 1,
        "createdAt": "2026-07-19T11:12:18"
      }]
    }
"""
