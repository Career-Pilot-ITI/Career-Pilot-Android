package com.iti.careerpilot.reports.data.datasource.remote

class ReportsDataNotFoundException(
    sessionId: String,
) : IllegalArgumentException("No report exists for session id: $sessionId")
