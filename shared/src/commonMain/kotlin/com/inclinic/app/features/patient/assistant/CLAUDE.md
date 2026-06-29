# features/patient/assistant — AI Asistente Virtual

Pantalla de chat consumiendo `POST /api/assistant/chat` de ClinicAI (Vercel AI SDK v6 UIMessageStream).

## Estructura

```
assistant/
  core/         — modelos sealed (AssistantMessage, AssistantStreamEvent, ActiveToolCall, ToolName)
                  + tool result DTOs + AssistantError + port (AssistantChatDataSource)
  application/  — SendAssistantMessageUseCase (validación empty + delegación)
  infrastructure/ — DTOs + UIMessageStreamParser puro + KtorAssistantChatDataSource
                    (preparePost + ByteReadChannel streaming)
  presentation/
    component/  — AssistantChatComponent (interfaz) + DefaultAssistantChatComponent
                  (Decompose, instanceKeeper, RetainedState snapshot, scope manual)
    ui/         — AssistantChatScreen + AssistantResultParsers + AssistantErrorExt
    ui/components/ — MessageBubble, ToolLoadingPill, DoctorCard, DoctorCardGrid,
                     SlotPicker, BookingSuccessCard, BookingFailCard, DisclaimerBanner,
                     ChatInputBar (9 componentes atómicos)
  di/           — AssistantChatModule (Koin)
```

## Reglas de wire format (Vercel AI SDK v6 UIMessageStream)

- Chunks: `data: {type, delta, toolCallId, toolName, input, output, errorText, finishReason}`
- Tipos relevantes: `text-delta`, `tool-call`, `tool-output-available`, `finish`, `error`
- Envelope chunks (`start`, `finish-step`, `step-start`, etc.) → `null` en parser, skipeados
- `tool-output-available` **NO trae `toolName`** → correlación por `toolCallId` vs `activeToolCall.toolName`
- `listSpecialties` NO muestra ningún UI (consumida silenciosamente por el backend)

## Reglas de negocio

- `patientId` NUNCA viaja del cliente — el backend lo inyecta desde el JWT
- Disclaimer "no es diagnóstico médico" visible siempre al abrir la pantalla + dismissible por sesión
- 429 countdown: `DefaultAssistantChatComponent.startCooldown(N)` decrementa `retryAfterSeconds` cada 1s vía `scope.launch` + `delay(1_000L)`
- 401 mid-stream → emite `SessionEvents.expired` → redirect a login
- `BookingResult.Ok` → texto placeholder "Pago pendiente: {path}" (payment screen v2, sin navegación)
- "Reservar" en DoctorCard → pre-llena input con `"Quiero agendar con {name}"`, NO auto-envía
- Todo el copy de UI está en español (Argentina/Perú neutral)

## Ciclo de vida del componente

- `CoroutineScope` creado manualmente con `dispatchers.main + SupervisorJob()` — `essenty-lifecycle-coroutines` NO es dependencia transitiva de Decompose 3.5.0 en este proyecto
- `scope.cancel()` en `doOnDestroy` — cancela `activeStreamJob` y `cooldownJob` automáticamente
- `RetainedState` (InstanceKeeper) guarda snapshot del estado al destruir → restaura al recrear (back-nav)

## Tests

```bash
# Compilar para verificar que el package no tiene errores (pre-existing ~50 errores en otros packages bloquean la ejecución completa):
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :shared:compileTestKotlinIosSimulatorArm64 2>&1 | grep "assistant"
# → sin output = cero errores en el package

# Cuando los pre-existing errors se resuelvan, ejecutar con:
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :shared:iosSimulatorArm64Test --tests "com.inclinic.app.features.patient.assistant.*"
```

Tests ubicados en `shared/src/commonTest/.../features/patient/assistant/`:
- `UIMessageStreamParserTest` — 20 escenarios del parser puro
- `KtorAssistantChatDataSourceTest` — 11 escenarios MockEngine (happy path + errores HTTP)
- `SendAssistantMessageUseCaseTest` — 5 escenarios (blank + delegación)
- `DefaultAssistantChatComponentTest` — 22 escenarios (estado, streaming, tools, cooldown timer × 3)
- `AssistantChatModuleTest` — 3 resoluciones Koin

**Cooldown timer tests (20-22)** usan `TestAppDispatchers(scheduler = testScheduler, useStandard = true)` + `advanceTimeBy` para controlar el tiempo virtual. Los tests 1-19 usan `UnconfinedTestDispatcher` (default).

## v2 backlog

- Persistir `conversationId` entre sesiones (multiplatform-settings)
- Lista de conversaciones (historial multi-convo)
- Payment screen real para `bookAppointment ok:true`
- Voice input
- Image attachments (foto de receta para que el asistente lea)
- Typing animation
- Encryption at rest para mensajes en local DB
