package com.inclinic.app.ui

/**
 * InClinic Design System — entry point and layer map.
 *
 * Layers (atomic design):
 *
 * ```
 * ui/
 * ├── theme/        Tokens + Theme provider
 * │   ├── AppColors      Palette extracted from ClinicAI web globals.css
 * │   ├── AppTypography  System-font text styles
 * │   ├── AppDimens      Spacing, radii, border widths
 * │   ├── AppShapes      Reusable RoundedCornerShape values
 * │   ├── AppOpacity     Alpha constants (disabled, overlay, etc.)
 * │   ├── AppMotion      Animation durations + easing
 * │   ├── AppElevation   Surface elevation levels
 * │   └── AppTheme       Provider + accessor object
 * ├── atoms/        Single-purpose primitives
 * │   ├── AppButton      Branded button (Navy / Outline / Ghost / Danger × Sm/Md/Lg)
 * │   ├── AppTextField   Labeled input with focus/error states
 * │   ├── AppLink        Tappable text link (muted vs emphasized)
 * │   ├── AppDivider     Hairline horizontal rule
 * │   ├── AppBadge       Small status pill
 * │   ├── SectionHeader  Title + optional subtitle
 * │   ├── ErrorBanner    Red-bg / red-text inline notice
 * │   └── LoadingOverlay Full-screen scrim with progress
 * ├── molecules/    Compositions of atoms
 * │   └── LabeledLinkRow  "Prefix? Action" centered row used in auth flows
 * └── templates/    Page-level scaffolds
 *     └── AuthScaffold    Sand bg + scrollable + max-width 480dp body
 * ```
 *
 * Conventions:
 * - All composables live in `shared/commonMain` so they compile for Android + iOS.
 * - Tokens are immutable data classes provided via CompositionLocal.
 * - Atoms never reach outside [AppTheme]; molecules / templates may compose them.
 * - No Material Icons dependency — use text or vector painters from `Res`.
 * - All UI copy is Spanish (per product language).
 */
@Suppress("unused")
private object DesignSystemMarker
