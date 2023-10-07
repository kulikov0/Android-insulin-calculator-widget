package kulikov.insulincalc

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kulikov.insulincalc.util.CalculatorElements
import kulikov.insulincalc.util.ParamsDb
import java.math.BigDecimal
import java.math.RoundingMode

class InsulinCalcWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ParamsDb.restoreValues(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        if (remoteViews == null) {
            remoteViews = RemoteViews(context.packageName, R.layout.widget_calc)
        }

        updateViews()

        setPendingIntentsToElements(context, remoteViews, appWidgetIds)

        val component = ComponentName(context, this::class.java)
        appWidgetManager.updateAppWidget(component, remoteViews)

    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == BUTTON_PRESS_ACTION)
            processButtonClick(context, intent)

    }

    private fun processButtonClick(context: Context, intent: Intent) {
        ParamsDb.restoreValues(context)
        if (currentContainerInfo == null) {
            currentContainerInfo = ParamsDb.restoreCurrentContainer(context)
        }
        when (val elementId = intent.extras?.getInt(ELEMENT_ID) ?: return) {

            R.id.tvDelete -> removeOneSymbol(context)

            R.id.tvEquals -> calculateTargetInsulin(context)

            else -> {
                if (elementId in CalculatorElements.containerIds) {
                    enableContainer(elementId, context)
                } else {
                    addSymbolToActiveContainer(elementId, context)
                }

            }
        }

        updateWidgets(context, intent)
    }

    @SuppressLint("StringFormatInvalid")
    private fun calculateTargetInsulin(context: Context) {

        ParamsDb.restoreValues(context)

        val currentSugarText = CalculatorElements.ContainerInfo.CurrentSugar.textValue
        val requiredSugarText = CalculatorElements.ContainerInfo.RequiredSugar.textValue
        val coefficient = CalculatorElements.ContainerInfo.Coefficient.textValue

        if (currentSugarText.endsWith(".") ||
            requiredSugarText.endsWith(".") ||
            coefficient.endsWith(".")
        ) return

        val currentSugarValue = currentSugarText.replace(",", ".").toDouble()
        val requiredSugarValue = requiredSugarText.replace(",", ".").toDouble()
        val coefficientValue = coefficient.replace(",", ".").toDouble()

        if (coefficientValue == .0)
            return

        val targetInsulinValue = (currentSugarValue - requiredSugarValue) / coefficientValue


        val roundedTargetInsulinValue = BigDecimal(targetInsulinValue)
            .setScale(3, RoundingMode.HALF_EVEN)

        var roundedTargetValue =
            "$roundedTargetInsulinValue".dropLastWhile { it == '0' || it == '.' }

        if (roundedTargetValue.isBlank())
            roundedTargetValue = "0"


        val finalValue = context.getString(R.string.placeholder_units, roundedTargetValue)

        CalculatorElements.ContainerInfo.Insulin.textValue = finalValue

    }

    private fun enableContainer(textId: Int, context: Context) {
        for (id in CalculatorElements.containerIds) {

            remoteViews?.setInt(
                CalculatorElements.getContainerBgId(id),
                "setBackgroundResource",
                R.color.white
            )
        }

        currentContainerInfo = if (textId != CalculatorElements.ContainerInfo.Insulin.textId) {
            remoteViews?.setInt(
                CalculatorElements.getContainerBgId(textId),
                "setBackgroundResource",
                R.drawable.bg_rounded_selected
            )

            CalculatorElements.ContainerInfo.fromTextId(textId)
        } else null

        ParamsDb.saveCurrentContainer(currentContainerInfo, context)

    }

    private fun updateWidgets(context: Context, intent: Intent) {
        val widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: return
        val appWidgetManager = AppWidgetManager.getInstance(context)
        onUpdate(context, appWidgetManager, widgetIds)
    }

    private fun removeOneSymbol(context: Context) {
        currentContainerInfo?.let { currentContainerInfo ->
            val currentText = currentContainerInfo.textValue
            val targetSymbol = when {
                currentText.length <= 1 -> "0"
                else -> currentText.substring(0, currentText.length - 1)
            }
            currentContainerInfo.textValue = targetSymbol

            ParamsDb.saveValues(context)

        }


    }

    private fun updateViews() {
        val reqSugarInfo = CalculatorElements.ContainerInfo.RequiredSugar
        val insulinInfo = CalculatorElements.ContainerInfo.Insulin
        val currentSugar = CalculatorElements.ContainerInfo.CurrentSugar
        val coefficientInfo = CalculatorElements.ContainerInfo.Coefficient

        remoteViews?.setTextViewText(
            reqSugarInfo.textId,
            reqSugarInfo.textValue
        )

        remoteViews?.setTextViewText(
            insulinInfo.textId,
            insulinInfo.textValue
        )

        remoteViews?.setTextViewText(
            currentSugar.textId,
            currentSugar.textValue
        )

        remoteViews?.setTextViewText(
            coefficientInfo.textId,
            coefficientInfo.textValue
        )

    }

    private fun addSymbolToActiveContainer(buttonId: Int, context: Context) {
        val symbol = CalculatorElements.getButtonSymbol(buttonId)

        currentContainerInfo?.let { currentContainerInfo ->
            val currentTextSymbol = currentContainerInfo.textValue
            val targetSymbol = when {
                symbol == "." && currentTextSymbol.contains(".") -> currentContainerInfo.textValue
                currentTextSymbol == "0" && symbol == "." -> "0."
                currentTextSymbol == "0" -> symbol
                else -> currentTextSymbol + symbol
            }

            currentContainerInfo.textValue = targetSymbol

            ParamsDb.saveValues(context)

        }


    }


    private fun setPendingIntentsToElements(
        context: Context,
        remoteViews: RemoteViews?,
        widgetIds: IntArray
    ) {
        for (element in CalculatorElements.elementIds) {
            remoteViews?.setOnClickPendingIntent(
                element,
                getElementPendingIntent(context, element, widgetIds)
            )
        }

        for (container in CalculatorElements.containerIds) {
            remoteViews?.setOnClickPendingIntent(
                container,
                getElementPendingIntent(context, container, widgetIds)
            )
        }
    }

    private fun getElementPendingIntent(
        context: Context,
        buttonId: Int,
        widgetIds: IntArray
    ): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = BUTTON_PRESS_ACTION
        intent.putExtra(ELEMENT_ID, buttonId)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        return PendingIntent.getBroadcast(
            context,
            buttonId,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    companion object {

        const val BUTTON_PRESS_ACTION = "button.press.action"
        const val ELEMENT_ID = "button.id"

        private var remoteViews: RemoteViews? = null

        private var currentContainerInfo: CalculatorElements.ContainerInfo? = null

    }

}