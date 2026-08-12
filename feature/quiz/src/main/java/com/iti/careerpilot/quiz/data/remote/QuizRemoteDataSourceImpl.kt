package com.iti.careerpilot.quiz.data.remote

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.iti.careerpilot.quiz.data.remote.QuizRemoteDataSourceImpl.Companion.SYSTEM_INSTRUCTION
import com.iti.careerpilot.quiz.data.remote.dto.LearningPointResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.QuizResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.TopicsResponseDto
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QuizRemoteDataSourceImpl @Inject constructor(
    private val json: Json
) : QuizRemoteDataSource {

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = MODEL_NAME,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content { text(SYSTEM_INSTRUCTION) }
    )

    override suspend fun generateTopics(
        track: String,
        seniority: String
    ): TopicsResponseDto {
        val prompt = """
            Track: $track
            Seniority: $seniority
            
            Generate a list of relevant technical study topics for this track and seniority.
            Each topic should have a unique id, title, and a brief description.
            Do not generate learning content or quizzes yet.
        """.trimIndent()

        val response = model.generateContent(prompt)
        return json.decodeFromString(response.text ?: throw Exception("Empty AI response"))
    }

    override suspend fun generateNextLearningPoint(
        track: String,
        seniority: String,
        topic: String,
        coveredConcepts: List<String>
    ): LearningPointResponseDto {
        val prompt = """
            Track: $track
            Seniority: $seniority
            Selected Topic: $topic
            Previously Covered Concepts: ${coveredConcepts.joinToString()}
            
            Identify the next important concept to teach within this topic.
            Avoid repeating concepts already covered.
            If the topic is sufficiently covered, set topicCompleted to true.
            Otherwise, provide the title, concise explanation, and a practical example for the next learning point.
        """.trimIndent()

        val response = model.generateContent(prompt)
        return json.decodeFromString(response.text ?: throw Exception("Empty AI response"))
    }

    override suspend fun generateQuiz(
        topic: String,
        learningPointTitle: String,
        learningPointExplanation: String,
        learningPointExample: String
    ): QuizResponseDto {
        val prompt = """
            Topic: $topic
            Learning Point: $learningPointTitle
            Explanation: $learningPointExplanation
            Example: $learningPointExample
            
            Generate 3 to 5 multiple-choice questions testing the user's understanding of the learning point above.
            Each question must have exactly 4 options, a correctAnswerIndex (0-3), and an explanation for the correct answer.
        """.trimIndent()

        val response = model.generateContent(prompt)
        return json.decodeFromString(response.text ?: throw Exception("Empty AI response"))
    }

    companion object {
        const val MODEL_NAME = "gemini-3.1-flash-lite"
        private const val SYSTEM_INSTRUCTION = """
            You are the technical learning engine for an AI-powered interview preparation application.
            Your job is to teach technical concepts progressively to a user preparing for technical interviews.
            The user has a specific career track and seniority level.

            Rules:
            1. Teach one important technical concept at a time.
            2. Every learning point must contain:
               - a concise explanation
               - a practical example
            3. Adapt explanations to the user's seniority.
            4. Start with foundational concepts and progress toward more advanced concepts.
            5. Do not repeat concepts that have already been covered.
            6. Only teach concepts relevant to the selected topic.
            7. Maintain logical progression between concepts.
            8. When all important concepts of the topic have been sufficiently covered, mark the topic as complete.
            9. Do not use a fixed number of learning points to determine completion.
            10. Quiz questions must test the learning point that was just taught.
            11. Generate between 3 and 5 multiple-choice questions.
            12. Questions should test understanding, not only memorization.
            13. Technical information must be accurate.
            14. Do not invent APIs, framework behavior, or language features.
            15. Return responses using the requested JSON schema.
            16. Never make navigation decisions.
            17. Never tell the application which screen to open.
        """
    }
}
