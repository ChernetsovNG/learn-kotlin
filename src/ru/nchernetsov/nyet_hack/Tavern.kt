package ru.nchernetsov.nyet_hack

fun main() {
    var beverage = readLine()?.let {
        if (it.isNotBlank()) {
            it.capitalize()
        } else {
            "Buttered Ale"
        }
    }
    println(beverage)
}
