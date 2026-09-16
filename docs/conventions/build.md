# 빌드

> 코드 예시는 규칙을 보여주기 위한 것이다. AGP·Gradle 버전에 따라 DSL이 다를 수 있으니 작성할 때 해당 버전 문서를 확인한다.

## 1. convention plugin

### 1-1. 공통 빌드 설정은 `build-logic`에만 둔다 `필수`

- 모듈 사이에 반복되는 빌드 설정은 included build인 `build-logic/convention`의 convention plugin으로 만든다.
- `buildSrc`를 쓰지 않는다. `buildSrc`를 고치면 전체 빌드 설정이 무효화된다.
- 플러그인은 `Plugin<Project>`를 구현한 클래스로 만들고, 여러 플러그인이 쓰는 설정은 `internal` 확장 함수(`configureKotlinAndroid()` 등)로 나눈다.

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("build-logic")
    ...
}
```

근거: [Gradle: Sharing build logic between subprojects](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html), [Modularization patterns: Keep your configuration consistent](https://developer.android.com/topic/modularization/patterns), NiA `build-logic`

### 1-2. 플러그인 목록 `필수`

| ID | 적용 모듈 | 하는 일 |
|---|---|---|
| `cherrypokemon.android.application` | `:app` | AGP application, SDK·Java·Kotlin 설정, Lint, Spotless |
| `cherrypokemon.android.application.compose` | `:app` | Compose 컴파일러, Compose BOM |
| `cherrypokemon.android.library` | 모든 Android library | AGP library, SDK·Java·Kotlin 설정, `resourcePrefix`, Lint, Spotless, 공통 테스트 의존성 |
| `cherrypokemon.android.library.compose` | `:core:designsystem`, `:core:ui`, `:feature:*:impl` | Compose 컴파일러, Compose BOM, stability configuration |
| `cherrypokemon.android.feature.api` | `:feature:*:api` | library + serialization 플러그인 + `api(projects.core.navigation)` |
| `cherrypokemon.android.feature.impl` | `:feature:*:impl` | library + Hilt + `:core:ui`·`:core:designsystem`·lifecycle·Navigation 3·Hilt ViewModel 의존성 |
| `cherrypokemon.android.room` | `:core:database` | Room 플러그인, KSP, 스키마 디렉터리 |
| `cherrypokemon.hilt` | Hilt를 쓰는 모듈 | KSP + Hilt (JVM 모듈은 `hilt-core`) |
| `cherrypokemon.jvm.library` | `:core:model`, `:core:common` | Kotlin JVM, Java·Kotlin 설정, Lint, Spotless |
| `cherrypokemon.android.lint` | 위 플러그인이 적용 | Lint 설정 |
| `cherrypokemon.root` | 루트 프로젝트 | 루트 수준 Spotless |

- 플러그인 클래스 이름은 `<대상><종류>ConventionPlugin`이다. (`AndroidFeatureImplConventionPlugin`)
- 플러그인을 추가하면 `gradle/libs.versions.toml`의 `[plugins]`에 버전 없이 등록한다.

```toml
[plugins]
cherrypokemon-android-library = { id = "cherrypokemon.android.library" }
```

근거: NiA `build-logic/convention/build.gradle.kts`

### 1-3. SDK와 언어 버전은 한 곳에서 `필수`

`compileSdk`, `minSdk`, `targetSdk`, Java `sourceCompatibility` / `targetCompatibility`, Kotlin `jvmTarget`은 `build-logic`의 설정 함수 한 곳에서만 정한다. 모듈 `build.gradle.kts`에 쓰지 않는다.

## 2. 모듈 build 파일

### 2-1. 담는 것 `필수`

모듈 `build.gradle.kts`에는 다음만 둔다.

1. `plugins { alias(libs.plugins.…) }`
2. `android { namespace = "…" }`
3. 그 모듈에만 필요한 `dependencies`

```kotlin
// feature/pokemondetail/impl/build.gradle.kts
plugins {
    alias(libs.plugins.cherrypokemon.android.feature.impl)
    alias(libs.plugins.cherrypokemon.android.library.compose)
}

android {
    namespace = "com.cherryzp.cherrypokemon.feature.pokemondetail.impl"
}

dependencies {
    implementation(projects.feature.pokemondetail.api)
    implementation(projects.core.data)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)

    testImplementation(projects.core.testing)
    testImplementation(libs.orbit.test)
    androidTestImplementation(projects.core.testing)
}
```

```kotlin
// ❌ 모듈마다 SDK·Java 설정을 복사하고, 플러그인 alias를 우회한다
plugins {
    id(libs.plugins.android.library.get().pluginId)
}
android {
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
```

### 2-2. 프로젝트 참조 `필수`

type-safe project accessor(`projects.core.data`)를 쓴다. `project(":core:data")` 문자열을 쓰지 않는다.

```kotlin
// settings.gradle.kts
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```

### 2-3. 리소스 접두사와 namespace `필수`

- 리소스 이름 충돌을 막기 위해 Android library는 `resourcePrefix`를 모듈 경로로 설정한다. (`:core:ui` → `core_ui_`) convention plugin이 자동으로 넣는다.
- `namespace`는 `com.cherryzp.cherrypokemon.` + 모듈 경로다.

근거: NiA `AndroidLibraryConventionPlugin`

## 3. 버전 카탈로그 `필수`

- 모든 라이브러리와 플러그인 버전은 `gradle/libs.versions.toml`에만 쓴다. build 파일에 버전 문자열을 쓰지 않는다.
- 동적 버전(`1.+`, `latest.release`)을 쓰지 않는다.
- BOM이 있는 라이브러리(Compose, OkHttp 등)는 BOM으로 버전을 맞춘다.
- 키 이름은 그룹과 아티팩트를 kebab-case로 잇는다. (`androidx-lifecycle-runtime-compose`, `hilt-android-testing`)
- `[bundles]`는 항상 함께 쓰는 라이브러리 묶음에만 쓴다.

```toml
[versions]
androidxNavigation3 = "…"

[libraries]
androidx-navigation3-runtime = { group = "androidx.navigation3", name = "navigation3-runtime", version.ref = "androidxNavigation3" }
```

근거: [Migrate your build to version catalogs](https://developer.android.com/build/migrate-to-catalogs)

## 4. 포맷 검사 `필수`

- Spotless + ktlint를 convention plugin으로 모든 모듈에 적용한다. ktlint는 Android 스타일(`android = true`)로 설정한다.
- 규칙 조정은 루트 `.editorconfig`에서만 한다.
- `./gradlew spotlessCheck`가 실패하면 머지하지 않는다. 고칠 때는 `./gradlew spotlessApply`.

근거: NiA `build-logic/.../Spotless.kt`, [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)

## 5. Lint `필수`

- Android Lint를 모든 모듈에 적용하고 `checkDependencies = true`로 모듈 사이 문제까지 검사한다.
- 새 경고를 lint baseline에 추가해서 숨기지 않는다.
- CI에서 release 변형으로 Lint를 실행한다.

근거: [Improve your code with lint checks](https://developer.android.com/studio/write/lint), NiA `AndroidLintConventionPlugin`

## 6. Gradle 설정 `필수`

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.code.style=official
```

- 저장소는 `settings.gradle.kts`의 `dependencyResolutionManagement`에서만 선언하고 `repositoriesMode = FAIL_ON_PROJECT_REPOS`로 둔다.
- `google()` 저장소는 `com.android.*`, `com.google.*`, `androidx.*` 그룹만 받도록 제한한다.
- JDK 버전 요구를 `settings.gradle.kts`에서 확인해 맞지 않으면 바로 실패하게 한다.

근거: [Optimize your build speed](https://developer.android.com/build/optimize-your-build), [Gradle configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html), NiA `settings.gradle.kts` / `gradle.properties`

## 7. 릴리즈 빌드 `필수`

- `isMinifyEnabled = true`, `isShrinkResources = true`, `proguard-android-optimize.txt`를 쓴다.
- 리플렉션에 기대는 코드를 만들지 않는다. 직렬화는 kotlinx.serialization, DI는 Hilt(컴파일 타임)로 해서 keep 규칙이 거의 필요 없게 한다.
- keep 규칙을 추가하면 이유를 주석으로 남긴다.
- 의존성을 바꾼 PR은 release 빌드를 만들어 실행해 본다.

근거: [Shrink, obfuscate, and optimize your app](https://developer.android.com/build/shrink-code)

## 8. CI `필수`

GitHub Actions에서 PR마다 다음을 실행한다. 하나라도 실패하면 머지하지 않는다.

| 단계 | 명령 |
|---|---|
| 포맷 | `./gradlew spotlessCheck` |
| 로컬 테스트 | `./gradlew testDebugUnitTest` |
| Lint | `./gradlew lintRelease` |
| 빌드 | `./gradlew assembleDebug assembleRelease` |
| 계측 테스트 | Gradle Managed Device로 `connectedDebugAndroidTest` 또는 managed device 태스크 |

근거: [Gradle Managed Devices](https://developer.android.com/studio/test/gradle-managed-devices), NiA `.github/workflows`
