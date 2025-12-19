package com.example.fauxtoes.core.network

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.Result
import retrofit2.Callback
import retrofit2.Response

internal class ResultCallAdapter<S : Any>(
    private val successType: Type,
) : CallAdapter<S, Call<Result<S>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<S>): Call<Result<S>> {
        return ResultCall(call)
    }
}

class ResultCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? =
        when (getRawType(returnType)) {

            Call::class.java -> {
                // get the response type inside the `Call` type
                val responseType = getParameterUpperBound(0, returnType as ParameterizedType)

                // if the response type is not Result then we can't handle this type, so we return null
                if (getRawType(responseType) == Result::class.java) {
                    val successBodyType =
                        getParameterUpperBound(0, responseType as ParameterizedType)
                    ResultCallAdapter<Any>(successBodyType)
                } else null
            }

            else -> null
        }
}

class ResultCall<T : Any>(
    private val delegate: Call<T>,
) : Call<Result<T>> {
    override fun enqueue(callback: Callback<Result<T>>) = synchronized(this) {
        delegate.enqueue(
            object : Callback<T> {

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    val result = if (response.isSuccessful && body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(HttpException(response))
                    }
                    callback.onResponse(this@ResultCall, Response.success(result))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    callback.onResponse(this@ResultCall, Response.success(Result.failure(t)))
                }
            }
        )
    }

    override fun isExecuted() = synchronized(this) { delegate.isExecuted }

    override fun isCanceled() = synchronized(this) { delegate.isCanceled }

    override fun clone() = ResultCall(delegate.clone())

    override fun cancel() = synchronized(this) { delegate.cancel() }

    override fun execute(): Response<Result<T>> {
        throw UnsupportedOperationException("NetworkResponseCall doesn't support execute")
    }

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

}

class HttpException(response: Response<*>) :
    Exception("HTTP ${response.code()} ${response.message()}")

