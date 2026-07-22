package com.iti.careerpilot.core.network

object Endpoints {
    const val BASE_URL = BuildConfig.BASE_URL

    const val UPLOAD_FILE = "$BASE_URL/api/v1/files/upload"
    const val PROFILE = "$BASE_URL/api/v1/profile"
    const val GET_TRACKS = "$BASE_URL/api/v1/tracks"
    const val REFRESH_TOKEN = "$BASE_URL/api/v1/auth/refresh"
    const val SEND_OTP = "$BASE_URL/api/v1/otp/send"
    const val VERIFY_OTP = "$BASE_URL/api/v1/otp/verify"
    const val INTERVIEW_SESSIONS = "$BASE_URL/api/v1/interviews/sessions"
    fun SUBMIT_ANSWER(sessionId: Long) = "$INTERVIEW_SESSIONS/$sessionId/answer"
    fun GET_SESSION_FEEDBACK(sessionId: Long) = "$INTERVIEW_SESSIONS/$sessionId/feedback"
    fun GET_SESSION_STATE(sessionId: Long) = "$INTERVIEW_SESSIONS/$sessionId/state"
}
