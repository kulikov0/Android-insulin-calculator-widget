package kulikov.insulincalc.util

import android.content.Context
import android.content.SharedPreferences

object ParamsDb {

    private const val ARG_PREFS = "arg_prefs"
    private const val ARG_REQ_SUG = "req_sugar"
    private const val ARG_CUR_SUG = "cur_sugar"
    private const val ARG_COEFFICIENT = "coef"
    private const val ARG_CURRENT_CONTAINER = "curr_cont"

    fun saveCurrentContainer(currentContainer: CalculatorElements.ContainerInfo?, context: Context) {
        getPrefs(context).edit().putInt(ARG_CURRENT_CONTAINER, currentContainer?.textId ?: -1).apply()
    }

    fun restoreCurrentContainer(context: Context): CalculatorElements.ContainerInfo? {
        val currentContainerId = getPrefs(context).getInt(ARG_CURRENT_CONTAINER, 0)
        return CalculatorElements.ContainerInfo.fromTextId(currentContainerId)
    }

    fun saveValues(context: Context) {
        val requiredSugar = CalculatorElements.ContainerInfo.RequiredSugar.textValue
        val currentSugar = CalculatorElements.ContainerInfo.CurrentSugar.textValue
        val coefficient = CalculatorElements.ContainerInfo.Coefficient.textValue

        val prefs = getPrefs(context)

        with(prefs.edit()) {
            putString(ARG_REQ_SUG, requiredSugar)
            putString(ARG_CUR_SUG, currentSugar)
            putString(ARG_COEFFICIENT, coefficient)
            apply()
        }
    }



    fun restoreValues(context: Context) {
        val prefs = getPrefs(context)
        with(prefs) {
            CalculatorElements.ContainerInfo.RequiredSugar.textValue = getString(ARG_REQ_SUG, "0") ?: "0"
            CalculatorElements.ContainerInfo.CurrentSugar.textValue = getString(ARG_CUR_SUG, "0") ?: "0"
            CalculatorElements.ContainerInfo.Coefficient.textValue = getString(ARG_COEFFICIENT, "0") ?: "0"
        }
    }

    fun clear(context: Context) {
       getPrefs(context).edit().clear().apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(ARG_PREFS, Context.MODE_PRIVATE)
    }

}