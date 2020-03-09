import ru.nchernetsov.List

fun main() {
    val f: (Int) -> (Int) -> Int = { x -> { y -> x * y } }
    println(f(2)(3))
    val oa = Option(2)
    val ob = Option(3)
    val oc = map2(oa, ob, f)
    println(oc)
}

fun <A> sequence(list: List<Option<A>>): Option<List<A>> =
    list.foldRight(Option(List())) { x ->
        { y: Option<List<A>> ->
            map2(x, y) { a ->
                { b: List<A> -> b.cons(a) }
            }
        }
    }

fun <A, B, C> map2(oa: Option<A>, ob: Option<B>, f: (A) -> (B) -> C): Option<C> =
    oa.flatMap { a -> ob.map { b -> f(a)(b) } }

fun <A, B> lift(f: (A) -> B): (Option<A>) -> Option<B> = {
    try {
        it.map(f)
    } catch (e: Exception) {
        Option()
    }
}

data class Toon(val firstName: String, val lastName: String, val email: Option<String>) {
    companion object {
        operator fun invoke(firstName: String, lastName: String, email: String? = null) =
            Toon(firstName, lastName, Option(email))
    }
}

sealed class Option<out A> {

    fun filter(p: (A) -> Boolean): Option<A> = flatMap { x -> if (p(x)) this else None }

    fun orElse(default: () -> Option<@UnsafeVariance A>): Option<A> = map { this }.getOrElse(default)

    fun <B> flatMap(f: (A) -> Option<B>): Option<B> = map(f).getOrElse { None }

    fun <B> map(f: (A) -> B): Option<B> = when (this) {
        is None -> None
        is Some -> Some(f(value))
    }

    fun getOrElse(default: () -> @UnsafeVariance A): A = when (this) {
        is None -> default()
        is Some -> value
    }

    abstract fun isEmpty(): Boolean

    internal object None : Option<Nothing>() {
        override fun isEmpty() = true
        override fun toString(): String = "None"
        override fun equals(other: Any?): Boolean = other === None
        override fun hashCode(): Int = 0
    }

    internal data class Some<out A>(internal val value: A) : Option<A>() {
        override fun isEmpty() = false
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Option<A> =
            when (a) {
                null -> None
                else -> Some(a)
            }
    }
}
