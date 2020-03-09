package ru.nchernetsov

fun main() {
    val list1: List<Int> = List(1, 2, 3)
    val list2: List<Int> = List(4, 5, 6)
    println(list1.concat(list2).map { it * it }.filter { it % 2 == 0 }.flatMap { i -> List(i, -i) })
}

sealed class List<out A> {
    fun <B> map(f: (A) -> B): List<B> = foldLeft(Nil) { acc: List<B> -> { h: A -> Cons(f(h), acc) } }.reverse()
    fun filter(p: (A) -> Boolean): List<A> = cofoldRight(Nil) { h -> { t: List<A> -> if (p(h)) Cons(h, t) else t } }
    fun <B> flatMap(f: (A) -> List<B>): List<B> = flatten(map(f))

    fun length(): Int = foldLeft(0) { { _ -> it + 1 } }

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = foldLeft(identity, this, f)
    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B = foldRight(this, identity, f)
    fun <B> cofoldRight(identity: B, f: (A) -> (B) -> B): B = cofoldRight(identity, this.reverse(), identity, f)

    fun init(): List<A> = when (this) {
        Nil -> throw IllegalStateException("init called on an empty list")
        is Cons -> reverse().drop(1).reverse()
    }

    fun reverse(): List<A> = foldLeft(invoke()) { acc -> { acc.cons(it) } }
    fun concat(list: List<@UnsafeVariance A>): List<A> = concat(this, list)
    fun dropWhile(p: (A) -> Boolean): List<A> = dropWhile(this, p)
    abstract fun drop(n: Int): List<A>
    fun cons(a: @UnsafeVariance A): List<A> = Cons(a, this)

    fun setHead(a: @UnsafeVariance A): List<A> = when (this) {
        Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> tail.cons(a)
    }

    abstract fun isEmpty(): Boolean

    internal object Nil : List<Nothing>() {
        override fun isEmpty() = true
        override fun toString(): String = "[NIL]"
        override fun drop(n: Int): List<Nothing> = this
    }

    internal class Cons<out A>(internal val head: A, internal val tail: List<A>) : List<A>() {
        override fun isEmpty() = false
        override fun toString(): String = "[${toString("", this)}NIL]"
        override fun drop(n: Int): List<A> {
            tailrec fun drop(n: Int, list: List<A>): List<A> =
                if (n <= 0) list else when (list) {
                    is Cons -> drop(n - 1, list.tail)
                    is Nil -> list
                }
            return drop(n, this)
        }

        private tailrec fun toString(acc: String, list: List<A>): String =
            when (list) {
                is Nil -> acc
                is Cons -> toString("$acc${list.head}, ", list.tail)
            }
    }

    companion object {
        operator fun <A> invoke(vararg az: A): List<A> =
            az.foldRight(Nil as List<A>) { a: A, list: List<A> ->
                Cons(a, list)
            }

        private tailrec fun <A> dropWhile(list: List<A>, p: (A) -> Boolean): List<A> = when (list) {
            Nil -> list
            is Cons -> if (p(list.head)) dropWhile(list.tail, p) else list
        }

        fun <A> concat(list1: List<A>, list2: List<A>): List<A> = when (list1) {
            Nil -> list2
            is Cons -> concat(list1.tail, list2).cons(list1.head)
        }

        tailrec fun <A> reverse(acc: List<A>, list: List<A>): List<A> =
            when (list) {
                Nil -> acc
                is Cons -> reverse(acc.cons(list.head), list.tail)
            }

        tailrec fun <A, B> foldLeft(acc: B, list: List<A>, f: (B) -> (A) -> B): B =
            when (list) {
                Nil -> acc
                is Cons -> foldLeft(f(acc)(list.head), list.tail, f)
            }

        fun <A, B> foldRight(list: List<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                Nil -> identity
                is Cons -> f(list.head)(foldRight(list.tail, identity, f))
            }

        fun sum(list: List<Int>): Int = list.foldLeft(0, { x -> { y -> x + y } })
        fun product(list: List<Int>): Int = list.foldLeft(1, { x -> { y -> x * y } })

        private tailrec fun <A, B> cofoldRight(acc: B, list: List<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                Nil -> acc
                is Cons -> cofoldRight(f(list.head)(acc), list.tail, identity, f)
            }

        fun <A> flatten(list: List<List<A>>): List<A> = list.cofoldRight(Nil) { x -> x::concat }
    }
}
