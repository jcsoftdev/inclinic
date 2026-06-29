# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

InClinic — Kotlin Multiplatform Mobile app targeting Android and iOS using Compose Multiplatform.

- Package: `com.inclinic.app`
- Kotlin: 2.3.21 | Compose Multiplatform: 1.10.3 | AGP: 8.11.2
- Android minSdk: 29 / compileSdk: 36 | iOS via Xcode

## Modules

| Module | Role |
|---|---|
| `shared` | Platform-agnostic business logic + expect/actual abstractions |
| `composeApp` | Compose Multiplatform UI + Android entry point (`MainActivity`) |
| `iosApp` | SwiftUI wrapper (`ContentView.swift`) that embeds the Compose layer |

## Common Commands

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Tests
./gradlew :shared:commonTest
./gradlew :composeApp:testDebugUnitTest

# Single test
./gradlew :shared:commonTest --tests "com.inclinic.app.ExampleTest"

# Full build
./gradlew build
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run from there.

## Architecture

The project follows a hexagonal / screaming architecture per vertical slice. The established pattern:

- **`shared/commonMain`** — business logic, domain models, repository interfaces, expect declarations
- **`shared/androidMain` / `shared/iosMain`** — actual implementations of platform APIs
- **`composeApp`** — Compose UI only; no business logic here

Platform abstractions use the **expect/actual** pattern (`Platform.kt` → `Platform.android.kt` / `Platform.ios.kt`).

**Stack in use**: Koin (DI), Ktor (networking), Decompose (navigation + state preservation via `instanceKeeper`), Turbine (Flow testing).

## Vertical Slices

| Feature | Path | Notes |
|---|---|---|
| Auth | `features/auth/` | Login, token storage, SessionEvents |
| Patient Home | `features/patient/presentation/` | Home screen + navigation flow |
| Patient Assistant Chat | `features/patient/assistant/` | AI chat via ClinicAI Vercel AI SDK v6 stream; see `assistant/CLAUDE.md` |

### Test filter — assistant package

Pre-existing ~50 compile errors in other packages block full test run. Use compilation check to verify the assistant package:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :shared:compileTestKotlinIosSimulatorArm64 2>&1 | grep "assistant"
# zero output = no errors in assistant package
```

## Dependency Management

All versions are centralized in `gradle/libs.versions.toml`. Always declare new dependencies there — never hardcode versions in module `build.gradle.kts` files.

<!-- project-brain:start -->
# Project: InClinic

This project is indexed with project-brain.

## project-brain MCP

Use the project-brain MCP tools (search_context, add_knowledge, list_modules, get_module, delete_knowledge, check_health) for codebase knowledge retrieval.

### Usage

Use `search_context` for semantic/conceptual or cross-file questions (when you don't know the exact symbol); for exact symbol/caller lookups prefer a structural/AST tool or grep. After `search_context`, use `expand_context(chunk_id)` to read full bodies instead of re-reading whole files.

### Project Context

- **Project ID**: InClinic
- **Stack**: Kotlin — frameworks: Jetpack Compose, Android

## Module Documentation

Detected modules:
- composeApp
- docs
- gradle
- iosApp
- shared

For each module stub in docs/modules/:
1. Read the stub file.
2. Fill in each section (Purpose, Key Files, Dependencies, Data Flow, Gotchas, Last Updated).
3. Call add_knowledge with the filled content, source: "docs/modules/<name>.md",
   tags: ["module", "<name>"].
Do this automatically when you open a project session and a stub is unfilled.

<!-- project-brain:end -->
