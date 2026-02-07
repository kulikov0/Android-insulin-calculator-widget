package kulikov.insulincalc.util

data class Injection(val timestamp: Long, val units: Double)

object IobCalculator {

    fun calculateIob(injections: List<Injection>, diaHours: Double): Double {
        val now = System.currentTimeMillis()
        return injections.sumOf { injection ->
            val elapsedHours = (now - injection.timestamp) / 3_600_000.0
            val remaining = injection.units * maxOf(0.0, 1.0 - elapsedHours / diaHours)
            remaining
        }
    }

    fun cleanExpiredInjections(injections: List<Injection>, diaHours: Double): List<Injection> {
        val now = System.currentTimeMillis()
        val diaMillis = (diaHours * 3_600_000).toLong()
        return injections.filter { now - it.timestamp < diaMillis }
    }
}
