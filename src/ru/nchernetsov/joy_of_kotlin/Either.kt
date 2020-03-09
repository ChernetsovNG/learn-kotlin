package ru.nchernetsov.joy_of_kotlin

sealed class Either<out A, out B> {

    internal class Left<out A, out B>(private val value: A) : Either<A, B>() {
        override fun toString(): String = "Left($value)"
    }

    internal class Right<out A, out B>(private val value: B) : Either<A, B>() {
        override fun toString(): String = "Right($value)"
    }

    companion object {
        fun <A, B> left(value: A): Either<A, B> =
            Left(value)
        fun <A, B> right(value: B): Either<A, B> =
            Right(value)
    }
}

fun <A : Comparable<A>> max(list: List<A>): Either<String, A> =
    when (list) {
        is List.Nil -> Either.left(
            "max called on an empty list"
        )
        is List.Cons -> Either.right(
            list.foldLeft(list.head) { x ->
                { y ->
                    if (x >= y) x else y
                }
            })
    }
