import SwiftUI
import Shared

/// Root SwiftUI view for the InClinic iOS app.
///
/// Bridges SwiftUI to the Compose Multiplatform UI via `MainViewControllerKt`.
/// The Koin graph is initialized in `iOSApp.init()` before this view appears,
/// so `createRootComponent()` can resolve its dependencies safely.
struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

/// UIViewControllerRepresentable that wraps the Compose login screen.
///
/// `createRootComponent()` builds a `DefaultRootComponent` (with a fresh Decompose
/// lifecycle) and `MainViewController(rootComponent:)` returns the backing
/// `UIViewController`. SwiftUI manages the container view lifecycle.
private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let root = MainViewControllerKt.createRootComponent()
        return MainViewControllerKt.MainViewController(rootComponent: root)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed — state is managed by the Decompose component.
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        // Preview not available for CMP-based views.
        Text("InClinic Login Preview")
    }
}
