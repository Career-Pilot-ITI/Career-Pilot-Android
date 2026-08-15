# CareerPilot - Video Interview Body Language Evaluation Specification

Comprehensive technical specification for on-device computer vision telemetry extraction, privacy architecture, cloud AI evaluation via **Firebase AI Logic (`gemini-3.6-flash`)**, and the deterministic **Local Heuristic Fallback Engine**.

---

## 1. Regulatory Compliance, Ethical Design & Product Positioning

### 1.1 EU AI Act Article 5(1)(f) Compliance & Product Scope
- **Strict Private Candidate-Facing Self-Practice & Coaching Tool**: CareerPilot is strictly an on-device, candidate-facing self-coaching application designed to help job seekers practice interview mechanics in private.
- **Zero Recruiter / Employer Integration**: CareerPilot does **not** feature recruiter dashboards, hiring management integrations, automated candidate screening, or decision-gating capabilities. Telemetry and evaluations are never transmitted to prospective employers or third parties.
- **No Gatekeeping or High-Risk Classification**: Because the system is exclusively candidate-facing and purely educational/advisory with zero impact on actual employment decisions, hiring processes, or candidate ranking, it does not fall under prohibited or high-risk recruitment AI categories under EU AI Act Article 5(1)(f).
- **Delivery Dynamics vs. Emotion Inference**: The 4th evaluation dimension is strictly framed as **"Facial Expressiveness & Delivery Dynamics"**. The system evaluates only observable physical delivery animation, facial movement fluidity during speech, and conversational energy. It does **not** perform psychological emotion recognition, sentiment inference, personality profiling, or internal state estimation.

### 1.2 Accessibility & Neurodiversity Notice
- **Non-normative Behavioral Baselines**: Non-verbal communication behaviors (such as eye contact frequency, hand gesture frequency, and posture stability) vary significantly across neurodivergent individuals (e.g., autistic eye contact patterns, ADHD movement profiles) and cultural backgrounds.
- **Coaching Suggestions Only**: All scores, observations, and recommendations produced by CareerPilot are baseline presentation suggestions intended for personal reflection. They are **not** measures of candidate intelligence, integrity, capability, professional competence, or character.
- **Candidate Agency**: Candidates are encouraged to adapt coaching feedback to their individual comfort levels, accessibility needs, and natural communication styles.

---

## 2. Privacy Architecture & Data Minimization

### 2.1 On-Device ML Pipeline & Data Minimization Payload
- **100% On-Device Computer Vision Processing**: Camera frames captured via CameraX are processed locally on the Android device using MediaPipe tasks (`FaceLandmarker`, `PoseLandmarker`, `HandLandmarker`). Video streams, camera frames, image bitmaps, or raw landmark pixel coordinates **never** leave the local device.
- **Derived Numerical Telemetry Payload Only**: Only aggregated scalar metrics (a compact JSON payload matching the `BodyLanguageMetrics` data class) are transmitted to Firebase AI Logic (`gemini-3.6-flash`).
- **Data Minimization Guarantee**: No biometric template, facial embedding, raw audio file, or visual recording is serialized, stored externally, or uploaded.

### 2.2 High-Level Architecture Diagram

```
Camera Frame (CameraX)
       │
       ▼ (Live Stream Preview & On-Device Analysis)
┌─────────────────────────────────────────────────────────────┐
│  MediaPipe On-Device ML Pipeline (Throttled per Task)       │
│  ├─ FaceLandmarker (8 FPS): Iris centering, Euler angles    │
│  ├─ PoseLandmarker (4 FPS): Torso lean, shoulder tilt, lean │
│  └─ HandLandmarker (4 FPS): Hand bounding box, face touches │
└─────────────────────────────────────────────────────────────┘
       │
       ▼ (Folded strictly during active answer recording windows)
┌─────────────────────────────────────────────────────────────┐
│  SessionAggregator + KeyMomentDetector                      │
│  Folded into immutable `BodyLanguageMetrics` data snapshot  │
└─────────────────────────────────────────────────────────────┘
       │
       ▼ (Derived Numbers-Only JSON Payload: No Frames / Bitmaps / Biometrics)
┌─────────────────────────────────────────────────────────────┐
│  EvaluateBodyLanguageUseCase                                │
│                                                             │
│  Is Candidate Detected? (face/pose >= 15%)                  │
│  ├── NO  ──► Local Engine (Instant 0 score out-of-frame)    │
│  └── YES ──► Check Remote Config Kill-Switch                │
│              ├── OFF ──► Local Engine (KILL_SWITCH_DISABLED)│
│              └── ON  ──► BodyLanguageAiEvaluator            │
│                          ├── Try: Firebase AI (Gemini Flash)│
│                          └── Catch Error/Timeout (>20s)     │
│                              └──► Local Engine (Fallback)   │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Telemetry Metrics Captured & Computed

All metrics are aggregated strictly **while the candidate is actively recording their answer** (`_isRecordingActive == true`):

| Dimension | Telemetry Field | Unit | Mathematical / Vision Definition |
| :--- | :--- | :--- | :--- |
| **Gaze & Eye Contact** | `eyeContactPercentage` | `%` | $\frac{\text{Frames with iris centered \& head facing camera}}{\text{Total detected face frames}} \times 100$ |
| | `timeLookingAwayMs` | `ms` | Cumulative continuous duration spent looking away from camera |
| | `faceLostCount` | `count` | Number of times the candidate's face exited the frame |
| **Posture & Alignment** | `averageTorsoLeanDeg` | `degrees` | Sagittal torso angle: $\arctan\left(\frac{\Delta z}{\Delta y}\right)$ between shoulder midpoint and hip midpoint ($+ = \text{forward lean}, - = \text{backward}$) |
| | `averageShoulderTiltDeg` | `degrees` | Coronal asymmetry: $\arctan\left(\frac{\Delta y}{\Delta x}\right)$ between left and right shoulders |
| | `slouchPercentage` | `%` | Percentage of pose frames where $\text{slouchScore} > 0.4$ (neck compression + backward slope) |
| | `postureChanges` | `count` | Discrete shifts between upright posture and slouched posture |
| **Hand Gestures & Fidgeting** | `handsVisiblePercentage` | `%` | Percentage of frames where hands are visible in the chest/camera frame |
| | `handToFaceTouchCount` | `count` | Number of pacifying gestures where hand landmarks enter within $15\%$ of normalized face center |
| | `fidgetScore` | `0.0 .. 1.0` | Velocity variance of hand wrist landmarks across consecutive frames |
| **Facial Expressiveness & Delivery Dynamics** | `averageSmile` | `0.0 .. 1.0` | Blendshape average: $\frac{\text{mouthSmileLeft} + \text{mouthSmileRight}}{2}$ representing facial delivery animation |
| | `maxSmile` | `0.0 .. 1.0` | Peak facial animation intensity captured during delivery |
| **Key Moments** | `keyMoments` | `list` | Timestamped events: `SMILE_PEAK`, `SLOUCH_START`, `HAND_TO_FACE_TOUCH`, `EYE_CONTACT_LOST` |

---

## 4. Evaluation Path A: Firebase AI Logic (`gemini-3.6-flash`)

The primary evaluation is conducted by **Firebase AI Logic** using `GenerativeBackend.googleAI()` with model `gemini-3.6-flash`.

### System Prompt Guidelines & Empirical Benchmarks
The AI acts as a senior executive interview coach applying evidence-based behavioral benchmarks without attempting emotion inference:

1. **Presence & Tracking Safety**:
   - If `faceDetectionPercentage < 15%` or `poseDetectionPercentage < 15%`, the candidate was not properly in camera frame. Score undetected categories as 0 and provide clear camera positioning tips in observations.
2. **Gaze & Eye Contact (Weight: 30%)**:
   - Optimal conversational range: 50%–75% of speaking time. Brief cognitive gaze shifts (1–2s) to formulate ideas are natural and positive.
   - Low eye contact (<40%): Flag as reduced camera engagement or reading off-screen notes.
   - Continuous stare (>85%): Flag as overly intense or rigid camera focus.
3. **Posture & Presence (Weight: 25%)**:
   - Engagement: Torso forward lean of +5° to +15° signals active posture engagement and attentiveness.
   - Slouching: Forward lean >20° or negative lean (< -5°) signals low energy or slouching.
   - Stability: Penalize frequent posture shifts (>4 shifts) or shoulder asymmetry (>6° tilt).
4. **Hand Gestures & Kinesics (Weight: 25%)**:
   - Purposeful Gestures: Hands visible 25%–50% in chest area for emphasis is optimal.
   - Pacifying Behaviors (High Penalty): Hand-to-face touches (chin, nose, neck, hair) distract from clear delivery.
   - Fidgeting: Repetitive finger tapping, pen clicking, or micro-movements (`fidgetScore > 0.30`) signal physical restlessness.
5. **Facial Expressiveness & Delivery Dynamics (Weight: 20%)**:
   - Dynamic animation: Expression variation and smile peaks during key emphasis points foster engaging delivery energy. Dynamic conversational animation is preferred over frozen or monotone facial delivery. Evaluated under the `facialExpression` JSON key for schema compatibility.
6. **Actionable Output**:
   - Ground observations strictly in the telemetry numbers.
   - Keep observations concise (1–2 sentences) and constructive.
   - Provide 3 prioritized, highly practical coaching tips.
   - Output raw valid JSON only matching `BodyLanguageEvaluation` schema.

### Structured JSON Response Schema
```json
{
  "schemaVersion": 1,
  "overallScore": 84,
  "eyeContact": {
    "score": 92,
    "observation": "Maintained 65% natural eye contact with healthy pauses.",
    "tip": "Keep looking directly into the lens when concluding key points."
  },
  "posture": {
    "score": 88,
    "observation": "Slight 8° forward lean showing active engagement.",
    "tip": "Ensure shoulders remain level when transitioning between answers."
  },
  "handGestures": {
    "score": 78,
    "observation": "Natural gestures used; 1 pacifying face touch detected.",
    "tip": "Rest hands comfortably on the desk to prevent touching your face."
  },
  "facialExpression": {
    "score": 82,
    "observation": "Pleasant neutral demeanor with authentic warm smile peaks.",
    "tip": "Maintain this approachable, confident expression."
  },
  "confidenceBand": "HIGH",
  "summary": "Strong professional presence demonstrating engagement and calm confidence.",
  "actionableTips": [
    "Position your camera directly at eye level.",
    "Rest hands in your lap or on the table when not gesturing."
  ]
}
```

---

## 5. Evaluation Path B: Local Heuristic Fallback Engine

When offline, timed out (>20s), or if Gemini encounters a network error, `LocalBodyLanguageFallbackEngine.kt` computes an instant deterministic evaluation.

### Continuous Piecewise Mathematical Scoring Equations

All scoring functions use continuous ($C^0$ smooth transition) piecewise equations.

#### 1. Eye Contact Score ($S_{\text{eye}}$)
For $E = \text{eyeContactPercentage} \in [0, 100]$:
$$\text{RawScore}(E) = \begin{cases} 
30 + 1.0 \times E & 0\% \le E < 40\% \quad (30\text{–}70) \\
70 + 1.8 \times (E - 40) & 40\% \le E < 50\% \quad (70\text{–}88) \\
88 + 0.40 \times (E - 50) & 50\% \le E \le 75\% \quad (\text{Optimal range}, 88\text{–}98) \\
98 - 1.12 \times (E - 75) & E > 75\% \quad (\text{Over-stare penalty})
\end{cases}$$
$$S_{\text{eye}} = \text{clamp}\Big(\text{round}(\text{RawScore}(E)), 0, 100\Big)$$

#### 2. Posture Score ($S_{\text{posture}}$)
- **Base Score**: $90$
- **Engagement Lean Bonus / Penalty**:
$$\text{LeanBonus} = \begin{cases}
+5 & +5^\circ \le \text{torsoLean} \le +15^\circ \\
-5 & \text{torsoLean} > +20^\circ \text{ or } \text{torsoLean} < -5^\circ \\
0 & \text{otherwise}
\end{cases}$$
- **Shoulder Tilt Penalty**: $\text{TiltPenalty} = 5 \text{ if } |\text{shoulderTilt}| > 6^\circ \text{ else } 0$
- **Posture Shift Penalty**: $\text{ShiftPenalty} = \min(\text{postureChanges} \times 2, 15)$
- **Slouch Deduction**: $\text{SlouchDeduction} = \lfloor\text{slouchPercentage} \times 0.8\rfloor$
$$S_{\text{posture}} = \text{clamp}(90 + \text{LeanBonus} - \text{TiltPenalty} - \text{ShiftPenalty} - \text{SlouchDeduction}, 0, 100)$$

#### 3. Hand Gestures Score ($S_{\text{hands}}$)
For $V = \text{handsVisiblePercentage} \in [0, 100]$:
$$\text{BaseScore}(V) = \begin{cases}
65 + \left(\frac{V}{25}\right) \times 27 & 0\% \le V < 25\% \quad (65\text{–}92) \\
92 & 25\% \le V \le 50\% \quad (\text{Optimal range}, 92) \\
92 - \left(\frac{V - 50}{50}\right) \times 17 & 50\% < V \le 100\% \quad (92\text{–}75)
\end{cases}$$
- **Pacifying Touch Penalty**: $\text{TouchPenalty} = \text{handToFaceTouchCount} \times 12$
- **Fidget Penalty**: $\text{FidgetPenalty} = \lfloor\text{fidgetScore} \times 35\rfloor \text{ if } \text{fidgetScore} > 0.30 \text{ else } 0$
$$S_{\text{hands}} = \text{clamp}\Big(\text{round}(\text{BaseScore}(V)) - \text{TouchPenalty} - \text{FidgetPenalty}, 15, 100\Big)$$

#### 4. Facial Expressiveness & Delivery Dynamics Score ($S_{\text{facial}}$)
For average facial delivery animation $A = \text{averageSmile} \in [0, 1]$ and smile emphasis peaks $P = \text{expressivePeaks}$:
- **Peak Bonus**: $\text{PeakBonus} = \min(P \times 6, 18)$
- **Animation Bonus**:
$$\text{AnimationBonus}(A) = \begin{cases}
\lfloor \frac{A}{0.08} \times 10 \rfloor & A < 0.08 \quad (0\text{–}10) \\
10 & 0.08 \le A \le 0.40 \quad (\text{Optimal range}, 10) \\
\lfloor 10 - \frac{A - 0.40}{0.20} \times 10 \rfloor & 0.40 < A \le 0.60 \quad (10\text{–}0) \\
0 & A > 0.60
\end{cases}$$
$$S_{\text{facial}} = \text{clamp}(78 + \text{PeakBonus} + \text{AnimationBonus}(A), 0, 100)$$

#### 5. Overall Composite Score
$$\text{Overall Score} = \text{clamp}\Big(\lfloor 0.30 \cdot S_{\text{eye}} + 0.25 \cdot S_{\text{posture}} + 0.25 \cdot S_{\text{hands}} + 0.20 \cdot S_{\text{facial}} \rfloor, 0, 100\Big)$$

---

## 6. Confidence Bands & Deterministic Fallthrough

The system calculates confidence bands deterministically based on metric tracking completeness, candidate visibility, and sub-score stability.

| Confidence Band | Primary Evaluation Condition | Local Fallback Evaluation Condition | UI Meaning |
| :--- | :--- | :--- | :--- |
| **`HIGH`** | Returned by AI when tracking quality is complete and non-verbal delivery is consistent. | $\text{Overall Score} \ge 70$ AND $\min(S_{\text{eye}}, S_{\text{posture}}, S_{\text{hands}}, S_{\text{facial}}) \ge 45$ | High-confidence assessment. Comprehensive non-verbal tracking with balanced dimensional scores. |
| **`MODERATE`** | Returned by AI when partial metric anomalies exist. | $50 \le \text{Overall Score} < 70$, OR $\text{Overall Score} \ge 70$ with any single sub-score $< 45$. | Moderate confidence. Minor tracking anomalies or slight imbalances in non-verbal dimensions. |
| **`LOW`** | Returned by AI or fallback when candidate is out of frame or metrics are incomplete. | $\text{Overall Score} < 50$ OR Candidate out of frame (`!isCandidateDetected`). | Low confidence. Candidate out of view, low lighting, or unstable camera tracking. |

---

## 7. Resilience & Fallback Triggers

| Trigger Condition | Evaluator Response | UI State Exposure |
| :--- | :--- | :--- |
| Candidate Out of Frame (`!isCandidateDetected`) | Instant on-device evaluation (Score: 0) | `BodyLanguageUiState.Success` with 0 score and camera alignment tip |
| Remote Config Kill Switch Disabled | Instant on-device evaluation | `BodyLanguageUiState.FallbackUsed(reason = KILL_SWITCH_DISABLED)` |
| AI Call Timeout (>20s) | On-device heuristic scoring | `BodyLanguageUiState.FallbackUsed(reason = TIMEOUT)` |
| AI JSON Deserialization Failure | On-device heuristic scoring | `BodyLanguageUiState.FallbackUsed(reason = PARSE_FAILURE)` |
| Network Offline / Socket Error | On-device heuristic scoring | `BodyLanguageUiState.FallbackUsed(reason = OFFLINE)` |
