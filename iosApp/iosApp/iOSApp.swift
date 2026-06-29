import SwiftUI
import Shared

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        // Initialize the Koin DI graph before any UI is rendered.
        // KoinHelper is an `object` in Kotlin, exported as a singleton to Swift.
        // Without SKIE: call via KoinHelper.shared.initKoin()
        // Note: SKIE is deferred to a future change (see design amendment #15).
        KoinHelper.shared.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    AppDelegate.handleUrl(url)
                }
        }
    }
}

// MARK: - Deep link handling

/// Parses inclinic:// URLs and forwards them to the KMP RootComponent.
///
/// TODO (Phase 4 follow-up): Expose `RootComponent` instance to Swift via a
/// shared KMP object or a SKIE-generated bridge so `handleDeepLink` can be
/// called directly. For now the stub logs the URL; wire up once the KMP/Swift
/// bridge pattern is established for this project.
class AppDelegate: NSObject, UIApplicationDelegate {

    static func handleUrl(_ url: URL) {
        guard url.scheme == "inclinic" else { return }
        // Bridge to KMP RootComponent.handleDeepLink will be wired in Phase 4 follow-up.
        // The URL scheme is registered and the OS correctly routes the link to this app.
        print("[InClinic] Deep link received: \(url.absoluteString)")
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        AppDelegate.handleUrl(url)
        return true
    }
}