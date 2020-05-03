package ru.nchernetsov.joy_of_kotlin

import ru.nchernetsov.joy_of_kotlin.Tree.Companion.balance
import kotlin.math.abs

fun main() {
    val tree = Tree(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    println(tree)
    println(tree.height)
    println(tree.size)

    val treeBalanced = balance(tree)
    println(treeBalanced)
    println(treeBalanced.height)
    println(treeBalanced.size)
}

sealed class Tree<out A : Comparable<@UnsafeVariance A>> {

    abstract fun isEmpty(): Boolean

    abstract val size: Int

    abstract val height: Int

    internal abstract val value: A

    internal abstract val left: Tree<A>

    internal abstract val right: Tree<A>

    abstract fun max(): Result<A>

    abstract fun min(): Result<A>

    abstract fun merge(tree: Tree<@UnsafeVariance A>): Tree<A>

    abstract fun <B> foldLeft(identity: B, f: (B) -> (A) -> B, g: (B) -> (B) -> B): B

    abstract fun <B> foldInOrder(identity: B, f: (B) -> (A) -> (B) -> B): B

    abstract fun <B> foldPreOrder(identity: B, f: (A) -> (B) -> (B) -> B): B

    abstract fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (A) -> B): B

    operator fun plus(a: @UnsafeVariance A): Tree<A> {
        fun plusUnBalanced(a: @UnsafeVariance A, t: Tree<A>): Tree<A> = when (t) {
            Empty -> T(Empty, a, Empty)
            is T -> when {
                a < t.value -> T(plusUnBalanced(a, t.left), t.value, t.right)
                a > t.value -> T(t.left, t.value, plusUnBalanced(a, t.right))
                else -> T(t.left, a, t.right)
            }
        }
        return plusUnBalanced(a, this).let {
            when {
                it.height > log2nlz(it.size) * 100 -> balance(it)
                else -> it
            }
        }
    }

    fun contains(a: @UnsafeVariance A): Boolean = when (this) {
        is Empty -> false
        is T -> when {
            a < value -> left.contains(a)
            a > value -> right.contains(a)
            else -> value == a
        }
    }

    fun remove(a: @UnsafeVariance A): Tree<A> = when (this) {
        Empty -> this
        is T -> when {
            a < value -> T(left.remove(a), value, right)
            a > value -> T(left, value, right.remove(a))
            else -> left.removeMerge(right)
        }
    }

    fun removeMerge(ta: Tree<@UnsafeVariance A>): Tree<A> = when (this) {
        Empty -> ta
        is T -> when (ta) {
            Empty -> this
            is T -> when {
                ta.value < value -> T(left.removeMerge(ta), value, right)
                else -> T(left, value, right.removeMerge(ta))
            }
        }
    }

    protected abstract fun rotateRight(): Tree<A>

    protected abstract fun rotateLeft(): Tree<A>

    abstract fun toListInOrderRight(): List<A>

    // Пустое дерево
    internal object Empty : Tree<Nothing>() {

        override fun isEmpty(): Boolean = true

        override fun toString(): String = "E"

        override val size: Int = 0

        override val height: Int = -1

        override fun max(): Result<Nothing> = Result.empty()

        override fun min(): Result<Nothing> = Result.empty()

        override fun merge(tree: Tree<Nothing>): Tree<Nothing> = tree

        override fun <B> foldLeft(identity: B, f: (B) -> (Nothing) -> B, g: (B) -> (B) -> B): B = identity

        override fun <B> foldInOrder(identity: B, f: (B) -> (Nothing) -> (B) -> B): B = identity

        override fun <B> foldPreOrder(identity: B, f: (Nothing) -> (B) -> (B) -> B): B = identity

        override fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (Nothing) -> B): B = identity

        override fun rotateRight(): Tree<Nothing> = this

        override fun rotateLeft(): Tree<Nothing> = this

        override fun toListInOrderRight(): List<Nothing> = List()

        override val value: Nothing by lazy {
            throw IllegalStateException("No value in Empty")
        }

        override val left: Tree<Nothing> by lazy {
            throw IllegalStateException("No left in Empty")
        }

        override val right: Tree<Nothing> by lazy {
            throw IllegalStateException("No right in Empty")
        }
    }

    // Непустое дерево
    internal class T<out A : Comparable<@UnsafeVariance A>>(
        override val left: Tree<A>,
        override val value: A,
        override val right: Tree<A>
    ) : Tree<A>() {

        override fun isEmpty(): Boolean = false

        override fun toString(): String = "(T $left $value $right)"

        override val size: Int = 1 + left.size + right.size

        override val height: Int = 1 + Math.max(left.height, right.height)

        override fun max(): Result<A> = right.max().orElse { Result(value) }

        override fun min(): Result<A> = left.min().orElse { Result(value) }

        override fun merge(tree: Tree<@UnsafeVariance A>): Tree<A> = when (tree) {
            Empty -> this
            is T -> when {
                tree.value > this.value ->
                    T(left, value, right.merge(T(Empty, tree.value, tree.right)))
                        .merge(tree.left)
                tree.value < this.value ->
                    T(left.merge(T(tree.left, tree.value, Empty)), value, right)
                        .merge(tree.right)
                else ->
                    T(left.merge(tree.left), value, right.merge(tree.right))
            }
        }

        override fun <B> foldLeft(identity: B, f: (B) -> (A) -> B, g: (B) -> (B) -> B): B =
            g(right.foldLeft(identity, f, g))(f(left.foldLeft(identity, f, g))(this.value))

        override fun <B> foldInOrder(identity: B, f: (B) -> (A) -> (B) -> B): B =
            f(left.foldInOrder(identity, f))(value)(right.foldInOrder(identity, f))

        override fun <B> foldPreOrder(identity: B, f: (A) -> (B) -> (B) -> B): B =
            f(value)(left.foldPreOrder(identity, f))(right.foldPreOrder(identity, f))

        override fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (A) -> B): B =
            f(left.foldPostOrder(identity, f))(right.foldPostOrder(identity, f))(value)

        override fun rotateRight(): Tree<A> = when (left) {
            Empty -> this
            is T -> T(left.left, left.value, T(left.right, value, right))
        }

        override fun rotateLeft(): Tree<A> = when (right) {
            Empty -> this
            is T -> T(T(left, value, right.left), right.value, right.right)
        }

        override fun toListInOrderRight(): List<A> = unBalanceRight(List(), this)
    }

    companion object {
        operator fun <A : Comparable<A>> invoke(): Tree<A> = Empty

        operator fun <A : Comparable<A>> invoke(vararg az: A): Tree<A> =
            az.foldRight(Empty, { a: A, tree: Tree<A> -> tree.plus(a) })

        private tailrec fun <A : Comparable<A>> unBalanceRight(acc: List<A>, tree: Tree<A>): List<A> =
            when (tree) {
                Empty -> acc
                is T -> when (tree.left) {
                    Empty -> unBalanceRight(acc.cons(tree.value), tree.right)
                    is T -> unBalanceRight(acc, tree.rotateRight())
                }
            }

        fun <A : Comparable<A>> balance(tree: Tree<A>): Tree<A> =
            balanceHelper(tree.toListInOrderRight().foldLeft(Empty) { t: Tree<A> ->
                { a: A -> T(Empty, a, t) }
            })

        private fun <A : Comparable<A>> balanceHelper(tree: Tree<A>): Tree<A> = when {
            !tree.isEmpty() && tree.height > log2nlz(tree.size) -> when {
                abs(tree.left.height - tree.right.height) > 1 -> balanceHelper(balanceFirstLevel(tree))
                else -> T(balanceHelper(tree.left), tree.value, balanceHelper(tree.right))
            }
            else -> tree
        }

        private fun <A : Comparable<A>> balanceFirstLevel(tree: Tree<A>): Tree<A> =
            unfold(tree) { t: Tree<A> ->
                when {
                    isUnBalanced(t) -> when {
                        tree.right.height > tree.left.height -> Result(t.rotateLeft())
                        else -> Result(t.rotateRight())
                    }
                    else -> Result()
                }
            }

        private fun <A> unfold(a: A, f: (A) -> Result<A>): A {
            tailrec fun <A> unfold(a: Pair<Result<A>, Result<A>>, f: (A) -> Result<A>): Pair<Result<A>, Result<A>> {
                val x = a.second.flatMap { f(it) }
                return if (x.isSuccess()) {
                    unfold(Pair(a.second, x), f)
                } else {
                    a
                }
            }
            return Result(a).let { unfold(Pair(it, it), f).second.getOrElse(a) }
        }

        private fun <A : Comparable<A>> isUnBalanced(tree: Tree<A>): Boolean = when (tree) {
            Empty -> false
            is T -> abs(tree.left.height - tree.right.height) > (tree.size - 1) % 2
        }

        fun log2nlz(n: Int): Int = when (n) {
            0 -> 0
            else -> 31 - Integer.numberOfLeadingZeros(n)
        }
    }
}
