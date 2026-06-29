# InClinic — Agent Guide

App móvil del ecosistema InClinic. Kotlin Multiplatform (Android + iOS) con Compose Multiplatform. Consume la API de ClinicAI.

## Stack

- Kotlin 2.3 · Compose Multiplatform · AGP 8.11
- Android minSdk 29 / compileSdk 36 · iOS vía Xcode
- Koin (DI) · Ktor (networking) · Decompose (navegación + state) · Turbine (test de Flow)
- Package: `com.inclinic.app`

## Módulos

| Módulo | Rol |
|--------|-----|
| `shared` | Lógica de negocio + expect/actual |
| `composeApp` | UI Compose + entry point Android (`MainActivity`) |
| `iosApp` | Wrapper SwiftUI que embebe la capa Compose |

## Comandos

```bash
./gradlew :composeApp:assembleDebug      # build Android
./gradlew :composeApp:installDebug       # instalar
./gradlew :shared:commonTest             # tests shared
./gradlew build                          # build completo
```

iOS: abrir `iosApp/iosApp.xcodeproj` en Xcode.

## MCP / Tooling

- **project-brain** — búsqueda semántica del código (por significado, no string). Usa `search_context` para preguntas conceptuales/cross-file; luego `expand_context(chunk_id)` para el cuerpo completo. Para símbolo exacto / quién-llama, usa grep.
- **engram** — memoria persistente entre sesiones. `mem_save` proactivo tras decisiones/bugs/convenciones; `mem_search` para recuperar contexto previo.

## Reglas para agentes

- **Fuente de verdad:** `CLAUDE.md` (arquitectura, slices, comandos). Léelo antes de tocar código.
- Arquitectura hexagonal / screaming por vertical slice. Lógica en `shared/commonMain`; UI sin lógica en `composeApp`.
- Plataforma vía `expect/actual` (`Platform.kt` → `Platform.android.kt` / `Platform.ios.kt`).
- Versiones centralizadas en `gradle/libs.versions.toml` — nunca hardcodear versiones en `build.gradle.kts`.
- No hacer build automático.
- API base local: `http://localhost:4005` (ClinicAI API; ver `local.properties`).
