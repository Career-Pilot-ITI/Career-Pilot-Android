package com.iti.careerpilot.quiz.data.remote

import com.iti.careerpilot.quiz.data.remote.dto.LearningPointDto
import com.iti.careerpilot.quiz.data.remote.dto.LearningPointResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.QuizQuestionDto
import com.iti.careerpilot.quiz.data.remote.dto.QuizResponseDto
import com.iti.careerpilot.quiz.data.remote.dto.StudyTopicDto
import com.iti.careerpilot.quiz.data.remote.dto.TopicsResponseDto
import com.iti.common.util.fakeDelay
import com.iti.common.util.shouldFail
import javax.inject.Inject

class FakeQuizRemoteDataSource @Inject constructor(
) : QuizRemoteDataSource {
    override suspend fun generateTopics(track: String, seniority: String): TopicsResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return TopicsResponseDto(
            topics = listOf(
                StudyTopicDto("1", "Activity Lifecycle", "Understanding activity states and transitions"),
                StudyTopicDto("2", "Fragments", "Learning modular UI and fragment transactions"),
                StudyTopicDto("3", "Dependency Injection", "Managing dependencies with Hilt/Dagger"),
                StudyTopicDto("4", "Jetpack Compose", "Building modern declarative UIs"),
                StudyTopicDto("5", "Coroutines & Flow", "Handling asynchronous programming and reactive streams"),
                StudyTopicDto("6", "WorkManager", "Scheduling background tasks effectively"),
                StudyTopicDto("7", "ViewModel & LiveData", "Managing UI-related data in a lifecycle-conscious way"),
                StudyTopicDto("8", "Data Persistence (Room)", "Local database management and caching strategies")
            )
        )
    }

    override suspend fun generateNextLearningPoint(
        track: String,
        seniority: String,
        topic: String,
        coveredConcepts: List<String>
    ): LearningPointResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")

        val learningPoints = mapOf(
            "Activity Lifecycle" to listOf(
                LearningPointDto("onCreate()", "Called when the activity is first created. This is where you should do all of your normal static set up.", "override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(R.layout.main_activity) }"),
                LearningPointDto("onStart()", "Called when the activity is becoming visible to the user.", "override fun onStart() { super.onStart() }"),
                LearningPointDto("onResume()", "Called when the activity will start interacting with the user. At this point your activity is at the top of the activity stack.", "override fun onResume() { super.onResume() }"),
                LearningPointDto("onPause()", "Called when the system is about to start resuming a previous activity. Use this to stop animations or other ongoing actions.", "override fun onPause() { super.onPause() }")
            ),
            "Fragments" to listOf(
                LearningPointDto("Fragment Lifecycle", "Fragments have their own lifecycle, but it's tied to their host activity.", "class MyFragment : Fragment(R.layout.fragment_my)"),
                LearningPointDto("FragmentManager", "The class responsible for performing actions on your app's fragments.", "parentFragmentManager.commit { replace(R.id.container, MyFragment()) }")
            ),
            "Dependency Injection" to listOf(
                LearningPointDto("Hilt Basics", "Hilt provides a standard way to incorporate Dagger dependency injection into an Android application.", "@HiltAndroidApp class MyApplication : Application()"),
                LearningPointDto("@Inject", "Use this annotation to tell Hilt how to provide instances of a class.", "class MyRepo @Inject constructor(private val api: Api)"),
                LearningPointDto("@Module", "Sometimes a type cannot be constructor-injected (like interfaces or external libraries). Use Hilt modules.", "@Module @InstallIn(SingletonComponent::class) object NetworkModule")
            ),
            "Jetpack Compose" to listOf(
                LearningPointDto("Composables", "Functions that define your app's UI programmatically.", "@Composable fun Greeting(name: String) { Text(text = \"Hello \$name!\") }"),
                LearningPointDto("State in Compose", "State in an app is any value that can change over time.", "var count by remember { mutableStateOf(0) }"),
                LearningPointDto("Modifiers", "Modifiers allow you to decorate or augment a composable.", "Text(\"Hello\", modifier = Modifier.padding(16.dp))")
            ),
            "Coroutines & Flow" to listOf(
                LearningPointDto("Suspend Functions", "Functions that can be paused and resumed later.", "suspend fun fetchData(): Data = withContext(Dispatchers.IO) { ... }"),
                LearningPointDto("Flow", "An asynchronous data stream that sequentially emits values.", "fun getNumbers(): Flow<Int> = flow { for (i in 1..3) emit(i) }"),
                LearningPointDto("collect", "Flows are cold, meaning the code inside the flow builder runs only when the flow is collected.", "viewModelScope.launch { repository.data.collect { value -> ... } }")
            )
        )

        val topicPoints = learningPoints[topic] ?: learningPoints["Activity Lifecycle"]!!
        val nextPoint = topicPoints.find { it.title !in coveredConcepts }

        return if (nextPoint != null) {
            LearningPointResponseDto(
                topicCompleted = false,
                coveredConcept = nextPoint.title,
                learningPoint = nextPoint
            )
        } else {
            LearningPointResponseDto(
                topicCompleted = true,
                coveredConcept = coveredConcepts.lastOrNull() ?: "",
                learningPoint = topicPoints.last()
            )
        }
    }

    override suspend fun generateQuiz(
        topic: String,
        learningPointTitle: String,
        learningPointExplanation: String,
        learningPointExample: String
    ): QuizResponseDto {
        fakeDelay()
        if (shouldFail()) throw Exception("Fake network error")
        return QuizResponseDto(
            questions = listOf(
                QuizQuestionDto(
                    "q1",
                    "What is the first callback in the Activity lifecycle?",
                    listOf("onStart()", "onCreate()", "onResume()", "onPause()"),
                    1,
                    "onCreate() is the first callback triggered when an activity is created."
                ),
                QuizQuestionDto(
                    "q2",
                    "Which component is used to manage UI-related data in a lifecycle-aware way?",
                    listOf("Activity", "Service", "ViewModel", "Fragment"),
                    2,
                    "ViewModel is designed to store and manage UI-related data so that the data survives configuration changes."
                ),
                QuizQuestionDto(
                    "q3",
                    "What is the recommended way to handle background tasks that need to be guaranteed to run?",
                    listOf("AsyncTask", "Thread", "WorkManager", "IntentService"),
                    2,
                    "WorkManager is the recommended solution for persistent work."
                ),
                QuizQuestionDto(
                    "q4",
                    "Which layout is most suitable for building complex, declarative UIs in modern Android?",
                    listOf("LinearLayout", "RelativeLayout", "ConstraintLayout", "Jetpack Compose"),
                    3,
                    "Jetpack Compose is Android's modern toolkit for building native UI."
                )
            )
        )
    }
}
