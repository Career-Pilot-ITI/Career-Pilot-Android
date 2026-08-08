# Career Pilot - Project Context, AGENTS.md & Feature Architecture Guide

This document combines **`AGENTS.md`**, full application context, structural hierarchy, architecture guidelines, and a complete step-by-step tutorial for building new feature modules consistent with existing standards.

---

## 1. AGENTS.md & Architecture Specification

```markdown
# Career Pilot - Project Context and Architecture Specification

## 1. Project Overview
Career Pilot is a modern Android application built using Kotlin and Jetpack Compose. The project follows a modular, layer-oriented clean architecture to separate concerns, improve build times, maintain testability, and ensure scalability.

## 2. Architecture & Modules Structure
The codebase is split into multi-module Gradle components:

### Build Logic & Foundation
- `build-logic`: Centralized Gradle Convention Plugins (*.gradle.kts) sharing Compose, Ktor network, database, testing, and feature configurations across modules.
- `:app`: The main application entry point, assembling all feature modules, top-level Navigation graph (Navigation 3), and application Hilt setup.

### Core Modules (`:core:*`)
- `:core:designsystem`: Application design system built on Material 3 Expressive. Includes custom themes, color tokens, typography, icons, and reusable UI components.
- `:core:network`: Network layer built with Ktor Client (OkHttp engine), JWT token refresh via Auth plugin, interceptors, central endpoint management (Endpoints.kt), and HTTP timeout configurations.
- `:core:common`: Shared utilities, Kotlin Extensions, custom Coroutine Dispatcher qualifiers (@Dispatcher(IO)), and global Application Scope (@ApplicationScope).
- `:core:model`: Shared pure Kotlin data models free of framework dependencies.
- `:core:database`: Local persistence layer powered by Room Database.
- `:core:datastore`: Key-value storage and user preference management using AndroidX DataStore.
- `:core:access`: Feature access authorization and subscription entitlement state. Provides CheckFeatureAccessUseCase, RefreshAccessUseCase, and DeductCoinsUseCase to gate features by plan (FREE, PLUS, MAX) with coin top-up support.
- `:core:whisper`: On-device speech recognition / audio processing engine integration.

### Feature Modules (`:feature:*`)
- `:feature:login`: Authentication, registration, OTP verification, and password reset flows.
- `:feature:onboarding`: User initial setup flow: selecting career tracks, profile configuration, and CV file uploading/analysis.
- `:feature:home`: Main dashboard, recommended learning tracks, quick stats, and primary navigation hub.
- `:feature:profile`: Viewing profile overview, skills, career progress, and settings.
- `:feature:editprofile`: Editing user profile details, updating avatar, and managing personal preferences.
- `:feature:practicesession`: AI-powered mock interview practice, real-time voice feedback, and question evaluation.
- `:feature:reports`: Analytics, interview performance insights, score breakdowns, and career feedback reports.
- `:feature:payment`: Subscriptions, dynamic Paymob payment processing, coin top-up wallet, checkout, and transaction history.
```

---

## 2. Tech Stack & Key Libraries

- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose + Material 3 Expressive
- **Navigation:** Navigation 3 (`androidx.navigation3`)
- **Dependency Injection:** Dagger Hilt (`@HiltViewModel`, `@AndroidEntryPoint`, `@Module`, `@Binds`, `@Provides`)
- **Networking:** Ktor Client v3 (OkHttp engine, ContentNegotiation `kotlinx.serialization`, JWT token refresh plugin)
- **Local Storage:** Room Database + DataStore Preferences
- **Asynchronous Flow:** Kotlin Coroutines & `StateFlow` / `SharedFlow` / `Channel`
- **Audio & Speech Engine:** `:core:whisper` integration
- **Image Loading:** Coil (`coil-network-ktor3`)
- **Animations:** Lottie Compose & Compose Transition Animations
- **Dates & Times:** `kotlinx-datetime`
- **Build System:** Gradle Kotlin DSL with Version Catalog (`libs.versions.toml`) & `build-logic` convention plugins

---

## 3. Directory & Module Structure Hierarchy

```
CareerPilot/
├── .agents/
│   └── AGENTS.md                       # Project architecture rules & context
├── build-logic/                        # Gradle Convention Plugins
│   └── convention/src/main/kotlin/com/iti/careerpilot/buildlogic/
│       ├── AndroidApplicationConventionPlugin.kt
│       ├── AndroidComposeConventionPlugin.kt
│       ├── AndroidHiltConventionPlugin.kt
│       ├── AndroidKtorConventionPlugin.kt
│       ├── AndroidLibraryConventionPlugin.kt
│       ├── FeatureDataConventionPlugin.kt
│       ├── FeatureDomainConventionPlugin.kt
│       └── FeaturePresentationConventionPlugin.kt
├── app/                                # App Module (Navigation graph setup & entry point)
│   └── src/main/java/com/iti/careerpilot/
│       ├── MainActivity.kt
│       └── navigation/
├── core/                               # Infrastructure Modules
│   ├── access/                         # Subscription entitlement & Feature Gating
│   ├── common/                         # Coroutine Dispatchers (@Dispatcher(IO)), Extensions
│   ├── database/                       # Room DB Entities & DAOs
│   ├── datastore/                      # DataStore Preferences & Token Persistence
│   ├── designsystem/                   # Design Tokens, M3 Expressive Theme & Components
│   ├── model/                          # Shared pure Kotlin models
│   ├── network/                        # Ktor Client configuration & Endpoints.kt
│   └── whisper/                        # Speech recognition engine
├── feature/                            # Feature Modules (Clean architecture per feature)
│   ├── editprofile/
│   ├── home/
│   ├── login/
│   ├── onboarding/
│   ├── payment/
│   ├── practicesession/
│   ├── profile/
│   └── reports/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

---

## 4. Coding Guidelines & Architecture Rules

1. **MVI Architecture Pattern:**
   - **`UiState`:** Single immutable `StateFlow` representing screen state.
   - **`UiIntent`:** Sealed interface/class representing all user interactions.
   - **`UiEffect`:** One-off side effects (snackbars, navigation triggers) sent via `Channel` and collected via `LaunchedEffect`.

2. **Stateless vs. Stateful Composable Separation:**
   - **Stateful Wrapper (`<ScreenName>Screen`):** Handles ViewModel injection via `hiltViewModel()`, safe state collection via `collectAsStateWithLifecycle()`, and side effect collection.
   - **Stateless Content (`<ScreenName>Content`):** Accepts pure state and event lambdas (`onIntent`). No ViewModel references. `@Preview` friendly.

3. **Coroutines & Dispatchers:**
   - Never hardcode `Dispatchers.IO` or `Dispatchers.Default`. Always inject `@Dispatcher(IO) ioDispatcher: CoroutineDispatcher`.
   - Never create custom global scopes. Always inject `@ApplicationScope scope: CoroutineScope`.

4. **Network & Timeout Rules:**
   - Default network timeout is 30 seconds (`30_000ms`).
   - Heavy AI endpoints (such as `/api/v1/profile/cv/analyze` or AI mock practice sessions) **MUST** set custom timeouts (`120_000ms`) per request.

---

## 5. Step-by-Step: Creating a New Feature (`:feature:<name>`)

### Step 1: Register Module
In `settings.gradle.kts`:
```kotlin
include(":feature:example")
```

### Step 2: Configure `build.gradle.kts`
In `feature/example/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.careerpilot.feature.data)
    alias(libs.plugins.careerpilot.feature.presentation)
    alias(libs.plugins.careerpilot.android.ktor)
    alias(libs.plugins.careerpilot.testing)
}

android {
    namespace = "com.iti.careerpilot.example"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:access"))
}
```

### Step 3: Implement Layer Packages
Inside `src/main/java/com/iti/careerpilot/example/`:
- `data/`: `remote/datasource/`, `dto/`, `repository/`
- `di/`: Hilt `@Module` binding interfaces to implementations
- `domain/`: `model/`, `repository/`, `usecase/`
- `presentation/`: `UiState`, `UiIntent`, `UiEffect`, `ViewModel`, `Screen` (Stateful), `Content` (Stateless)

---

### Step 4: Code Standard Example

#### 1. Use Case (`domain/usecase/GetExampleDataUseCase.kt`)
```kotlin
package com.iti.careerpilot.example.domain.usecase

import com.iti.careerpilot.example.domain.repository.ExampleRepository
import javax.inject.Inject

class GetExampleDataUseCase @Inject constructor(
    private val repository: ExampleRepository
) {
    suspend operator fun invoke() = repository.getExampleData()
}
```

#### 2. Remote Data Source (`data/remote/datasource/ExampleRemoteDataSourceImpl.kt`)
```kotlin
package com.iti.careerpilot.example.data.remote.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.plugins.timeout
import com.iti.careerpilot.network.Endpoints
import com.iti.careerpilot.example.data.remote.dto.ExampleDto
import javax.inject.Inject

class ExampleRemoteDataSourceImpl @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun fetchData(): ExampleDto {
        return httpClient.get("${Endpoints.BASE_URL}/api/v1/example") {
            timeout { requestTimeoutMillis = 30_000 }
        }.body()
    }
}
```

#### 3. Repository (`data/repository/ExampleRepositoryImpl.kt`)
```kotlin
package com.iti.careerpilot.example.data.repository

import com.iti.careerpilot.common.dispatcher.Dispatcher
import com.iti.careerpilot.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.careerpilot.example.data.remote.datasource.ExampleRemoteDataSourceImpl
import com.iti.careerpilot.example.domain.repository.ExampleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExampleRepositoryImpl @Inject constructor(
    private val remoteDataSource: ExampleRemoteDataSourceImpl,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) : ExampleRepository {
    override suspend fun getExampleData() = withContext(ioDispatcher) {
        remoteDataSource.fetchData().toDomain()
    }
}
```

#### 4. Hilt Module (`di/ExampleModule.kt`)
```kotlin
package com.iti.careerpilot.example.di

import com.iti.careerpilot.example.data.repository.ExampleRepositoryImpl
import com.iti.careerpilot.example.domain.repository.ExampleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExampleModule {
    @Binds
    @Singleton
    abstract fun bindExampleRepository(impl: ExampleRepositoryImpl): ExampleRepository
}
```

#### 5. ViewModel (`presentation/ExampleViewModel.kt`)
```kotlin
package com.iti.careerpilot.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.careerpilot.example.domain.usecase.GetExampleDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val getExampleDataUseCase: GetExampleDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<ExampleUiEffect>()
    val effect = _effectChannel.receiveAsFlow()

    fun onIntent(intent: ExampleUiIntent) {
        when (intent) {
            is ExampleUiIntent.Load -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { getExampleDataUseCase() }
                .onSuccess { data -> _uiState.update { it.copy(isLoading = false, data = data) } }
                .onFailure { error -> 
                    _uiState.update { it.copy(isLoading = false) }
                    _effectChannel.send(ExampleUiEffect.ShowToast(error.message ?: "Error"))
                }
        }
    }
}
```

#### 6. Stateful Screen & Stateless Content (`presentation/ExampleScreen.kt`)
```kotlin
package com.iti.careerpilot.example.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ExampleScreen(
    viewModel: ExampleViewModel = hiltViewModel(),
    onNavigateNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExampleUiEffect.NavigateNext -> onNavigateNext()
                is ExampleUiEffect.ShowToast -> { /* handle toast */ }
            }
        }
    }

    ExampleContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun ExampleContent(
    state: ExampleUiState,
    onIntent: (ExampleUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Text(text = "Data Loaded Successfully")
        }
    }
}
```

---

## 6. Verification Checklist for New Feature Development
- [ ] Registered in `settings.gradle.kts`
- [ ] `build.gradle.kts` uses convention plugins
- [ ] Clean Architecture layer breakdown (`data`, `di`, `domain`, `presentation`)
- [ ] Injected `@Dispatcher(IO)` for background tasks
- [ ] Timeout overriden to `120_000ms` for AI operations
- [ ] MVI architecture (`UiState`, `UiIntent`, `UiEffect`)
- [ ] Stateful `Screen` wrapper separated from Stateless `Content`
- [ ] Feature entitlement gating (`:core:access`) integrated if required
- [ ] Navigation 3 entry wired in `:app` module
