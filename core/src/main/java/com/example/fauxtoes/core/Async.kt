package com.example.fauxtoes.core

sealed class Async<out T>(
    private val loading:Boolean
){
    val isLoading = loading

    data class Success<out T>(
        val result:T
    ): Async<T>(loading = false)

    data class Fail<out T>(
        val error:Throwable
    ):Async<T>(loading = false)

    data object Loading : Async<Nothing>(loading = true)

    data object Uninitialized : Async<Nothing>(loading = false)
}

class AsyncException(message: String, cause: Throwable? = null) :
    Throwable(message = message, cause = cause)

fun <T> Result<T>.toAsync() = this.getOrNull()
    ?.let { Async.Success<T>(it) }
    ?: Async.Fail(
        exceptionOrNull() ?: AsyncException(message = "Async failed without throwing exception")
    )