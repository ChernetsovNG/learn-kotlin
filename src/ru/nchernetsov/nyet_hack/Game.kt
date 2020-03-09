package ru.nchernetsov.nyet_hack

fun main() {
    val name = "Madrigal"
    val healthPoints = 89
    val isBlessed = true
    val isImmortal = false

    val race = "gnome"
    val faction = when (race) {
        "dwarf" -> "Keepers of the Mines"
        "gnome" -> "Keepers of the Mines"
        "orc" -> "Free People of the Rolling Hills"
        "human" -> "Free People of the Rolling Hills"
        else -> "unknown"
    }

    val auraVisible = isBlessed && healthPoints > 50 || isImmortal
    val auraColor = if (auraVisible) "GREEN" else "NONE"

    val healthStatus = when (healthPoints) {
        100 -> "is in excellent condition!"
        in 90..99 -> "has a few scratches"
        in 75..89 -> if (isBlessed) {
            "has some minor wounds but is healing quite quickly!"
        } else {
            "has some minor wounds"
        }
        in 15..74 -> "looks pretty hurt"
        else -> "is in awful condigion!"
    }

    println("(Aura: $auraColor) (Blessed: ${if (isBlessed) "YES" else "NO"})")
    println("$name $healthStatus")
}
