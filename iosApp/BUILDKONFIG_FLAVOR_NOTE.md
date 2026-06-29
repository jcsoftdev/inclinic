# BuildKonfig Flavor — Manual Xcode Step Required

The Xcode build phase "Compile Kotlin Framework" runs:

```sh
cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

This does NOT pass a BuildKonfig flavor, so the generated `BuildKonfig` object will have empty
`API_BASE_URL` and `ENVIRONMENT` fields. The Koin gatekeeper in `AuthModule` will crash at startup.

## How to fix

Open `iosApp/iosApp.xcodeproj` in Xcode, go to:
**Target "iosApp" → Build Phases → "Compile Kotlin Framework"**

Change the shell script from:
```sh
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

To:
```sh
./gradlew :shared:embedAndSignAppleFrameworkForXcode -Pbuildkonfig.flavor=dev
```

Adjust the flavor (`dev`, `staging`, `prod`) to match your target environment.

## Why this is manual

The `project.pbxproj` file is an Xcode-managed binary format that can cause merge conflicts
when edited programmatically. The change is a one-liner inside the "Compile Kotlin Framework"
shell script build phase — safe to make directly in Xcode UI.
