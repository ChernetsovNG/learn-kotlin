package ru.nchernetsov

import java.io.Serializable

fun main() {
    fun <K, V> Map<K, V>.getResult(key: K) = when {
        this.containsKey(key) -> Result(this[key])
        else -> Result.failure("Key $key not found in map")
    }
}

sealed class Result<out A> : Serializable {

    fun <A, B, C> map2(a: Result<A>, b: Result<B>, f: (A) -> (B) -> C): Result<C> = lift2(f)(a)(b)

    fun <A, B> lift(f: (A) -> B): (Result<A>) -> Result<B> = { it.map(f) }

    fun <A, B, C> lift2(f: (A) -> (B) -> C): (Result<A>) -> (Result<B>) -> Result<C> =
            { a ->
                { b ->
                    a.map(f).flatMap { b.map(it) }
                }
            }

    abstract fun forEachOrElse(onSuccess: (A) -> Unit, onFailure: (RuntimeException) -> Unit, onEmpty: () -> Unit)

    abstract fun forEach(effect: (A) -> Unit)

    fun exists(p: (A) -> Boolean): Boolean = map(p).getOrElse(false)

    fun filter(p: (A) -> Boolean): Result<A> = flatMap {
        if (p(it))
            this
        else
            failure("Condition not matched")
    }

    fun filter(message: String, p: (A) -> Boolean): Result<A> = flatMap {
        if (p(it))
            this
        else
            failure(message)
    }

    fun orElse(defaultValue: () -> Result<@UnsafeVariance A>): Result<A> =
            when (this) {
                is Success -> this
                is Failure -> try {
                    defaultValue()
                } catch (e: RuntimeException) {
                    failure(e)
                } catch (e: Exception) {
                    failure(RuntimeException(e))
                }
                is Empty -> Empty
            }

    fun getOrElse(defaultValue: @UnsafeVariance A): A = when (this) {
        is Success -> this.value
        is Failure -> defaultValue
        is Empty -> defaultValue
    }

    abstract fun <B> map(f: (A) -> B): Result<B>
    abstract fun <B> flatMap(f: (A) -> Result<B>): Result<B>

    internal class Success<out A>(internal val value: A) : Result<A>() {
        override fun forEachOrElse(onSuccess: (A) -> Unit, onFailure: (RuntimeException) -> Unit, onEmpty: () -> Unit) =
                onSuccess(value)

        override fun forEach(effect: (A) -> Unit) {
            effect(value)
        }

        override fun <B> map(f: (A) -> B): Result<B> = try {
            Success(f(value))
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = try {
            f(value)
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun toString(): String = "Success(${value}"
    }


    internal class Failure<out A>(internal val exception: RuntimeException) : Result<A>() {
        override fun forEachOrElse(onSuccess: (A) -> Unit, onFailure: (RuntimeException) -> Unit, onEmpty: () -> Unit) =
                onFailure(exception)

        override fun forEach(effect: (A) -> Unit) {}
        override fun <B> map(f: (A) -> B): Result<B> = Failure(exception)
        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = Failure(exception)
        override fun toString(): String = "Failure(${exception.message}"
    }

    internal object Empty : Result<Nothing>() {
        override fun forEachOrElse(
                onSuccess: (Nothing) -> Unit, onFailure: (RuntimeException) -> Unit,
                onEmpty: () -> Unit
        ) = onEmpty()

        override fun forEach(effect: (Nothing) -> Unit) {}
        override fun <B> map(f: (Nothing) -> B): Result<B> = Empty
        override fun <B> flatMap(f: (Nothing) -> Result<B>): Result<B> = Empty
        override fun toString(): String = "Empty"
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> Success(a)
        }

        fun <A> failure(message: String): Result<A> = Failure(IllegalStateException(message))

        fun <A> failure(exception: RuntimeException): Result<A> = Failure(exception)

        fun <A> failure(exception: Exception): Result<A> = Failure(IllegalStateException(exception))
    }
}
