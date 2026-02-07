package kulikov.insulincalc.util

import kulikov.insulincalc.R

object CalculatorElements {

    private val ELEMENTS: Map<Int, String> = mapOf(
        R.id.tv0 to "0",
        R.id.tv1 to "1",
        R.id.tv2 to "2",
        R.id.tv3 to "3",
        R.id.tv4 to "4",
        R.id.tv5 to "5",
        R.id.tv6 to "6",
        R.id.tv7 to "7",
        R.id.tv8 to "8",
        R.id.tv9 to "9",
        R.id.tvDot to ".",
        R.id.tvDelete to "",
        R.id.tvEquals to "",
        R.id.tvInject to ""
    )

    private val CONTAINERS: Map<Int, Int> = mapOf(
        R.id.tvRequiredSugar to R.id.containerRequiredSugar,
        R.id.tvCurrentSugar to R.id.containerCurrentSugar,
        R.id.tvCoefficient to R.id.containerCoefficient,
        R.id.tvInsulin to R.id.containerInsulin
    )

    fun getButtonSymbol(buttonId: Int): String {
        return ELEMENTS[buttonId].toString()
    }

    fun getContainerBgId(containerId: Int): Int {
        return CONTAINERS[containerId]!!
    }

    val elementIds: Iterable<Int>
        get() = ELEMENTS.keys

    val containerIds: Iterable<Int>
        get() = CONTAINERS.keys

    enum class ContainerInfo(val textId: Int, var textValue: String) {
        RequiredSugar(R.id.tvRequiredSugar, "0"),
        CurrentSugar(R.id.tvCurrentSugar, "0"),
        Coefficient(R.id.tvCoefficient, "0"),
        Insulin(R.id.tvInsulin, "");

        companion object {
            fun fromTextId(id: Int): ContainerInfo? {
                return when (id) {
                    R.id.tvRequiredSugar -> RequiredSugar
                    R.id.tvCurrentSugar -> CurrentSugar
                    R.id.tvCoefficient -> Coefficient
                    R.id.tvInsulin -> Insulin
                    else -> null
                }
            }
        }
    }
}