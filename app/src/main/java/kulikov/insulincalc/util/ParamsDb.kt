package kulikov.insulincalc.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object ParamsDb {

    private const val ARG_PREFS = "arg_prefs"
    private const val ARG_REQ_SUG = "req_sugar"
    private const val ARG_CUR_SUG = "cur_sugar"
    private const val ARG_COEFFICIENT = "coef"
    private const val ARG_CURRENT_CONTAINER = "curr_cont"
    private const val ARG_INJECTIONS = "injections"
    private const val ARG_DIA_HOURS = "dia_hours"
    private const val ARG_IOB_ENABLED = "iob_enabled"
    const val DEFAULT_DIA = 4.0

    fun saveCurrentContainer(currentContainer: CalculatorElements.ContainerInfo?, context: Context) {
        getPrefs(context).edit { putInt(ARG_CURRENT_CONTAINER, currentContainer?.textId ?: -1) }
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

        prefs.edit {
            putString(ARG_REQ_SUG, requiredSugar)
            putString(ARG_CUR_SUG, currentSugar)
            putString(ARG_COEFFICIENT, coefficient)
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

    fun saveIobEnabled(enabled: Boolean, context: Context) {
        getPrefs(context).edit { putBoolean(ARG_IOB_ENABLED, enabled) }
    }

    fun restoreIobEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(ARG_IOB_ENABLED, false)
    }

    fun saveDiaHours(diaHours: Double, context: Context) {
        getPrefs(context).edit { putFloat(ARG_DIA_HOURS, diaHours.toFloat()) }
    }

    fun restoreDiaHours(context: Context): Double {
        return getPrefs(context).getFloat(ARG_DIA_HOURS, DEFAULT_DIA.toFloat()).toDouble()
    }

    fun saveInjections(injections: List<Injection>, context: Context) {
        val jsonArray = JSONArray()
        for (injection in injections) {
            val obj = JSONObject()
            obj.put("timestamp", injection.timestamp)
            obj.put("units", injection.units)
            jsonArray.put(obj)
        }
        getPrefs(context).edit { putString(ARG_INJECTIONS, jsonArray.toString()) }
    }

    fun restoreInjections(context: Context): List<Injection> {
        val json = getPrefs(context).getString(ARG_INJECTIONS, null) ?: return emptyList()
        val jsonArray = JSONArray(json)
        val list = mutableListOf<Injection>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(Injection(obj.getLong("timestamp"), obj.getDouble("units")))
        }
        return list
    }

    fun addInjection(injection: Injection, context: Context) {
        val diaHours = restoreDiaHours(context)
        val injections = restoreInjections(context).toMutableList()
        injections.add(injection)
        val cleaned = IobCalculator.cleanExpiredInjections(injections, diaHours)
        saveInjections(cleaned, context)
    }

    fun updateLastInjectionTime(newTimestamp: Long, context: Context) {
        val injections = restoreInjections(context).toMutableList()
        if (injections.isEmpty()) return
        val lastIndex = injections.lastIndex
        injections[lastIndex] = injections[lastIndex].copy(timestamp = newTimestamp)
        saveInjections(injections, context)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(ARG_PREFS, Context.MODE_PRIVATE)
    }

}