package com.iti.careerpilot.core.network

object Endpoints {
    const val BASE_URL = BuildConfig.BASE_URL

    const val UPLOAD_FILE = "${BASE_URL}/api/v1/files/upload"
    const val PROFILE = "${BASE_URL}/api/v1/profile"
    const val GET_TRACKS = "${BASE_URL}/api/v1/tracks"
    const val REFRESH_TOKEN = "${BASE_URL}/api/v1/auth/refresh"
    const val SEND_OTP = "${BASE_URL}/api/v1/otp/send"
    const val VERIFY_OTP = "${BASE_URL}/api/v1/otp/verify"

    // Wallet
    const val WALLET_BALANCE = "${BASE_URL}/api/v1/wallet/balance"
    const val WALLET_TOP_UP = "${BASE_URL}/api/v1/wallet/top-up"
    const val WALLET_COIN_PACKS = "${BASE_URL}/api/v1/wallet/coin-packs"

    // Payments
    const val PAYMENT_INITIATE = "${BASE_URL}/api/v1/payments/initiate"
    const val PAYMENT_HISTORY = "${BASE_URL}/api/v1/payments/history"

    // Subscriptions
    const val SUBSCRIPTION_CURRENT = "${BASE_URL}/api/v1/subscriptions/current"
    const val SUBSCRIPTION_UPGRADE = "${BASE_URL}/api/v1/subscriptions/upgrade"
    const val SUBSCRIPTION_DOWNGRADE = "${BASE_URL}/api/v1/subscriptions/downgrade"
    const val SUBSCRIPTION_CANCEL = "${BASE_URL}/api/v1/subscriptions/cancel"
    const val SUBSCRIPTION_TIERS = "${BASE_URL}/api/v1/subscriptions/tiers"

    const val ANALYZE_CV = "$BASE_URL/api/v1/profile/cv/analyze"

    const val ATS_WORKSPACES = "$BASE_URL/api/v1/workspaces"
    const val ATS_IMPORT_JOB_URL = "$ATS_WORKSPACES/import/url"
    fun atsWorkspace(workspaceId: Long) = "$ATS_WORKSPACES/$workspaceId"
    fun atsScoreCv(workspaceId: Long) = "${atsWorkspace(workspaceId)}/score-cv"
    fun atsOptimizeCv(workspaceId: Long) = "${atsWorkspace(workspaceId)}/cv/optimize"
    fun atsCoverLetter(workspaceId: Long) = "${atsWorkspace(workspaceId)}/cover-letter"
    const val AI_JOBS = "$BASE_URL/api/v1/ai-jobs"
    fun aiJob(jobId: Long) = "$AI_JOBS/$jobId"

    const val INTERVIEW_SESSIONS = "$BASE_URL/api/v1/interviews/sessions"
    fun SUBMIT_ANSWER(sessionId: Long) = "$INTERVIEW_SESSIONS/$sessionId/answer"
    fun GET_SESSION_FEEDBACK(sessionId: Long) = "$INTERVIEW_SESSIONS/$sessionId/feedback"
    fun GET_SESSION_STATE(sessionId: Long) = "$INTERVIEW_SESSIONS/$sessionId/state"

    fun interviewSession(sessionId: Long): String = "$INTERVIEW_SESSIONS/$sessionId"

    fun interviewSessionFeedback(sessionId: Long): String =
        "${interviewSession(sessionId)}/feedback"

    fun interviewSessionQuestions(sessionId: Long): String =
        "${interviewSession(sessionId)}/questions"

    // Company / Applicant Interview Assessment
    const val APPLICANT_INTERVIEW = "$BASE_URL/api/v1/applicant/interview"
    fun applicantInterviewMetadata(token: String) = "$APPLICANT_INTERVIEW/$token"
    fun applicantVerifyEmail(token: String) = "$APPLICANT_INTERVIEW/$token/verify-email"
    fun applicantStartSession(token: String) = "$APPLICANT_INTERVIEW/$token/start"
    fun applicantSubmitAnswer(token: String) = "$APPLICANT_INTERVIEW/$token/answer"
    fun applicantCompleteSession(token: String) = "$APPLICANT_INTERVIEW/$token/complete"
    fun applicantSessionState(token: String) = "$APPLICANT_INTERVIEW/$token/state"
}


