# 빌드

> 코드 예시는 규칙을 보여주기 위한 것이다. AGP·Gradle 버전에 따라 DSL이 다르니 작성할 때 해당 버전 문서를 확인한다.

## 1. convention plugin

### 1-1. 공통 빌드 설정은 convention plugin으로 `필수`

- 모듈 사이에 반복되는 빌드 설정은 convention plugin으로 만든다. `subprojects {}` / `allprojects {}`로 다른 프로젝트를 설정하지 않는다.
- convention plugin은 included build인 `build-logic/convention`에 둔다. `buildSrc`를 쓰지 않는다. `buildSrc` 코드를 바꾸면 설정 단계가 무효화되고 모든 태스크를 다시 실행한다.
- 플러그인은 `Plugin<Project>`를 구현한 클래스(`<대상><종류>ConventionPlugin`)로 만들고, 여러 플러그인이 쓰는 설정은 `internal` 확장 함수로 나눈다.

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("build-logic")
}
```

근거: [Modularization patterns: Keep your configuration consistent](https://developer.android.com/topic/modularization/patterns) — "Use convention plugins to share build logic between modules", [Gradle: Sharing build logic](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html) — "Changes to code in buildSrc will invalidate the configuration phase", "Avoid cross-project configuration using subprojects and allprojects", NiA `build-logic`

### 1-2. 플러그인 목록 `필수`

| ID | 적용 모듈 | 하는 일 |
|---|---|---|
| `cherrypokemon.android.application` | `:app` | AGP application, SDK·Java·Kotlin 설정, Lint, 포맷 검사 |
| `cherrypokemon.android.application.compose` | `:app` | Compose 컴파일러, Compose BOM |
| `cherrypokemon.android.library` | 모든 Android library | AGP library, SDK·Java·Kotlin 설정, `resourcePrefix`, Lint, 포맷 검사, 공통 테스트 의존성 |
| `cherrypokemon.android.library.compose` | `:core:designsystem`, `:core:ui`, `:feature:*:impl` | Compose 컴파일러, Compose BOM, stability configuration file |
| `cherrypokemon.android.feature.api` | `:feature:*:api` | library + serialization 플러그인 + `:core:navigation` |
| `cherrypokemon.android.feature.impl` | `:feature:*:impl` | library + Hilt + `:core:ui`·`:core:designsystem`·lifecycle·Navigation 3·Hilt ViewModel 의존성 |
| `cherrypokemon.android.room` | `:core:database` | Room Gradle Plugin, KSP, 스키마 디렉터리 |
| `cherrypokemon.hilt` | Hilt를 쓰는 모듈 | KSP + Hilt (JVM 모듈은 `hilt-core`) |
| `cherrypokemon.jvm.library` | `:core:model`, `:core:common` | Kotlin JVM, Java·Kotlin 설정, Lint, 포맷 검사 |
| `cherrypokemon.android.lint` | 위 플러그인이 적용 | Lint 설정 |

근거: NiA `build-logic/convention`

### 1-3. SDK와 언어 버전 `필수`

- `compileSdk`와 `targetSdk`는 최신 SDK로 둔다. (`targetSdk`는 최소한 Google Play 요구 수준)
- `compileSdk`, `minSdk`, `targetSdk`, Java 호환 버전, Kotlin `jvmTarget`은 `build-logic`의 설정 함수 한 곳에서만 정한다. 모듈 `build.gradle.kts`에 쓰지 않는다.

근거: [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality): Target_SDK_Version, Compile_SDK_Version("App is built with the latest Android SDK"), NiA `KotlinAndroid.kt`

## 2. 모듈 build 파일 `필수`

모듈 `build.gradle.kts`에는 플러그인, `android { namespace }`, 그 모듈에만 필요한 의존성만 둔다.

```kotlin
// feature/pokemondetail/impl/build.gradle.kts
plugins {
    id("cherrypokemon.android.feature.impl")
    id("cherrypokemon.android.library.compose")
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
}
```

| 규칙 | 근거 |
|---|---|
| 버전 카탈로그의 플러그인은 `alias(libs.plugins.…)`, 카탈로그에 없는 convention plugin은 `id("…")`로 적용한다 | [Version catalogs](https://developer.android.com/build/migrate-to-catalogs): "Use alias for plugins that come from the version catalog file and id for plugins that don't come from the version catalog file, such as convention plugins." |
| 의존은 `implementation`. `api`는 다른 모듈에 그 API를 노출해야 할 때만. `runtimeOnly`는 거의 쓰지 않는다 | [Dependencies](https://developer.android.com/build/dependencies) |
| 프로젝트 의존은 type-safe accessor(`projects.core.data`) | NiA `settings.gradle.kts` (`TYPESAFE_PROJECT_ACCESSORS`) |
| `namespace`는 `com.cherryzp.cherrypokemon.` + 모듈 경로 | [kotlin.md 1-2](kotlin.md#1-2-패키지-필수) |
| Android library는 모듈 경로로 `resourcePrefix`를 둔다(`:core:ui` → `core_ui_`). convention plugin이 넣는다 | NiA `AndroidLibraryConventionPlugin` |

```kotlin
// ❌ 모듈마다 설정을 복사하고, 카탈로그 alias를 우회한다
plugins { id(libs.plugins.android.library.get().pluginId) }
android { compileOptions { sourceCompatibility = JavaVersion.VERSION_17 } }
```

## 3. 버전 카탈로그 `필수`

- 모든 라이브러리·플러그인 버전은 `gradle/libs.versions.toml`(기본 이름)에만 쓴다. build 파일에 버전 문자열을 쓰지 않는다.
- 키 이름은 kebab-case다. (`androidx-navigation3-runtime`)
- 동적 버전(`1.+`)을 쓰지 않는다.

근거: [Version catalogs](https://developer.android.com/build/migrate-to-catalogs): "The recommended naming for dependencies block in catalogs is kebab case", [Dependencies](https://developer.android.com/build/dependencies) Caution: "you shouldn't use dynamic version numbers"

## 4. Gradle 설정 `권장`

| 규칙 | 근거 |
|---|---|
| Android Studio, SDK 도구, AGP를 최신으로 유지한다 | [Optimize your build speed](https://developer.android.com/build/optimize-your-build) |
| 어노테이션 처리는 kapt가 아니라 KSP | 같음 |
| `org.gradle.configuration-cache=true` (플러그인 호환 확인 후) | 같음 |
| `android.enableJetifier=false` | 같음 |
| 저장소 선언에서 `gradlePluginPortal()`은 마지막 | 같음 |
| `org.gradle.parallel=true`, `org.gradle.caching=true` | NiA `gradle.properties` |
| 저장소는 `settings.gradle.kts`의 `dependencyResolutionManagement`에서만 선언(`FAIL_ON_PROJECT_REPOS`) | NiA `settings.gradle.kts` |

## 5. 포맷 검사 `필수`

- Spotless + ktlint를 convention plugin으로 모든 모듈에 적용한다. ktlint는 Android 스타일(`android = true`)로 설정한다.
- `spotlessCheck`가 실패하면 머지하지 않는다.

근거: NiA `build-logic/.../Spotless.kt` (모든 모듈에 적용)

## 6. Lint `필수`

- Android Lint를 모든 모듈에 적용하고 `checkDependencies = true`로 둔다.
- lint baseline은 기존 이슈를 담을 때만 쓴다. 새 이슈는 baseline에 추가하지 않고 고친다.
- CI에서 release 변형으로 Lint를 실행한다.

근거: [Improve your code with lint checks](https://developer.android.com/studio/write/lint): baseline은 "only new issues are reported"하기 위한 기능, NiA `AndroidLintConventionPlugin`

## 7. 릴리즈 빌드 `필수`

- release 빌드는 항상 R8 최적화를 켠다. 테스트·라이브러리 빌드에는 켜지 않는다.
  - AGP 9.3 이상: `buildTypes { release { optimization { enable = true } } }`, keep 규칙은 `src/<variant>/keepRules/*.keep`
  - AGP 9.3 미만: `isMinifyEnabled = true`, `isShrinkResources = true`, `proguard-android-optimize.txt`
- keep 규칙은 최소한으로 둔다. 리플렉션에 기대는 라이브러리 대신 코드 생성 방식(kotlinx.serialization, Hilt)을 쓴다.
- 프로덕션 빌드에 디버그 라이브러리를 넣지 않는다.

근거: [Enable app optimization with R8](https://developer.android.com/build/shrink-code) Important: "You should always enable optimization for your app's release build", "Refine keep rules to allow maximum optimization", [Core app quality](https://developer.android.com/docs/quality-guidelines/core-app-quality): Production_Build_Quality

## 8. CI `필수`

GitHub Actions에서 실행한다. 실패하면 머지하지 않는다.

| 시점 | 단계 |
|---|---|
| PR마다 | `spotlessCheck`, `testDebugUnitTest`, `lintRelease`, `assembleDebug`, `assembleRelease` |
| 머지 전 | Gradle Managed Device(Automated Test Device)로 계측 테스트 |

- GitHub Actions처럼 하드웨어 렌더링이 없는 환경에서는 `-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect`를 붙인다.

근거: [Testing strategies: Test infrastructure](https://developer.android.com/training/testing/fundamentals/strategies), [Gradle Managed Devices](https://developer.android.com/studio/test/gradle-managed-devices)
