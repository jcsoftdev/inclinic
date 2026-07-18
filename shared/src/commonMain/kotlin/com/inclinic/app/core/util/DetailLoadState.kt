package com.inclinic.app.core.util

/**
 * Pure, Compose-free view-state for "load one entity by id" screens
 * (admin appointment/doctor/pending-doctor detail, resolve-dispute, resolve-no-show).
 *
 * Factoring this out lets 5 screens share one `when` shape instead of each
 * re-deriving `isLoading` / `error` / `notFound` / `value` combinations inline —
 * see gap 4 of the design-gap-closure pass (DetailErrorState atom is the
 * matching UI half, in `ui/atoms/DetailErrorState.kt`).
 */
sealed interface DetailLoadState<out T> {
    data object Loading : DetailLoadState<Nothing>
    data class NotFound(val message: String) : DetailLoadState<Nothing>
    data class Failed(val message: String) : DetailLoadState<Nothing>
    data class Content<T>(val value: T) : DetailLoadState<T>
}

/**
 * Resolves the four detail states from the raw component fields.
 *
 * Precedence: loading wins first (even if a stale [value]/[error] lingers from a
 * previous attempt), then a present [value] always wins over any error (so a
 * background refresh failure after data already loaded doesn't blank the screen —
 * mirrors the "distinct from loaded state" requirement), then [notFound] vs a
 * generic [Failed].
 */
fun <T> detailLoadState(
    isLoading: Boolean,
    value: T?,
    error: String?,
    notFound: Boolean,
): DetailLoadState<T> = when {
    isLoading -> DetailLoadState.Loading
    value != null -> DetailLoadState.Content(value)
    notFound -> DetailLoadState.NotFound(error ?: "No encontrado.")
    error != null -> DetailLoadState.Failed(error)
    else -> DetailLoadState.Loading
}
