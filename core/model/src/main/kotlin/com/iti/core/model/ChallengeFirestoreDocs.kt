package com.iti.core.model

/**
 * Firestore Database Structure for Challenges
 *
 * 1. public_challenges (Collection)
 *    - {challengeId} (Document): Challenge object
 *      - Includes creatorId (email), trackId, questions, etc.
 *
 * 2. private_challenges (Collection)
 *    - {challengeId} (Document): Challenge object
 *
 * 3. challenge_sessions (Collection)
 *    - {sessionId} (Document): ChallengeSession object
 *      - challengeId (String): Reference to the challenge
 *      - participantEmail (String): User who took the challenge
 *      - score (Int): Total score
 *      - feedback (String): AI feedback
 *      - timestamp (Long): Completion date
 *      - results (List<ChallengeQuestionResult>): Question-by-question breakdown
 */
