package com.inclinic.app.core.error

sealed class ApiError : Throwable() {
    data object Network : ApiError()
    data object Timeout : ApiError()
    data object Unauthorized : ApiError()
    data object Forbidden : ApiError()
    data object NotFound : ApiError()
    data class Conflict(override val message: String?, val code: String? = null) : ApiError()
    data class BadRequest(override val message: String?, val code: String? = null) : ApiError()
    data class Server(val status: Int, val raw: String? = null) : ApiError()
}

sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data class Err(val error: ApiError) : ApiResult<Nothing>()
}

inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Ok) block(value)
    return this
}

inline fun <T> ApiResult<T>.onError(block: (ApiError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Err) block(error)
    return this
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Ok -> ApiResult.Ok(transform(value))
    is ApiResult.Err -> this
}

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Ok)?.value
