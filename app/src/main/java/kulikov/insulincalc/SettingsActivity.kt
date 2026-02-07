package kulikov.insulincalc

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kulikov.insulincalc.util.ParamsDb
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchIobEnabled: SwitchCompat
    private lateinit var etDiaHoursWhole: AppCompatEditText
    private lateinit var etDiaMinutes: AppCompatEditText
    private lateinit var tvLastInjectionTime: AppCompatTextView
    private lateinit var btnEditLastInjection: AppCompatButton
    private lateinit var btnSave: AppCompatButton
    private lateinit var btnClearHistory: AppCompatButton

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollSettings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom)
            )
            insets
        }

        switchIobEnabled = findViewById(R.id.switchIobEnabled)
        etDiaHoursWhole = findViewById(R.id.etDiaHoursWhole)
        etDiaMinutes = findViewById(R.id.etDiaMinutes)
        tvLastInjectionTime = findViewById(R.id.tvLastInjectionTime)
        btnEditLastInjection = findViewById(R.id.btnEditLastInjection)
        btnSave = findViewById(R.id.btnSave)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        switchIobEnabled.isChecked = ParamsDb.restoreIobEnabled(this)
        val savedDia = ParamsDb.restoreDiaHours(this)
        val wholeHours = savedDia.toInt()
        val minutes = ((savedDia - wholeHours) * 60).toInt()
        etDiaHoursWhole.setText(wholeHours.toString())
        etDiaMinutes.setText(minutes.toString())

        updateLastInjectionDisplay()

        btnEditLastInjection.setOnClickListener {
            showDateTimePicker()
        }

        btnSave.setOnClickListener {
            val hours = etDiaHoursWhole.text.toString().toIntOrNull()
            val mins = etDiaMinutes.text.toString().toIntOrNull() ?: 0
            if (hours == null || (hours == 0 && mins == 0) || mins < 0 || mins >= 60) {
                etDiaHoursWhole.error = "Invalid"
                return@setOnClickListener
            }
            val diaHours = hours + mins / 60.0
            ParamsDb.saveIobEnabled(switchIobEnabled.isChecked, this)
            ParamsDb.saveDiaHours(diaHours, this)
            notifyWidgetUpdate()
            finish()
        }

        btnClearHistory.setOnClickListener {
            ParamsDb.saveInjections(emptyList(), this)
            updateLastInjectionDisplay()
            notifyWidgetUpdate()
            Toast.makeText(this, getString(R.string.history_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLastInjectionDisplay() {
        val injections = ParamsDb.restoreInjections(this)
        if (injections.isEmpty()) {
            tvLastInjectionTime.text = getString(R.string.no_injections)
        } else {
            val last = injections.last()
            tvLastInjectionTime.text = dateFormat.format(last.timestamp)
        }
    }

    private fun showDateTimePicker() {
        val injections = ParamsDb.restoreInjections(this)
        if (injections.isEmpty()) return

        val last = injections.last()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = last.timestamp

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        ParamsDb.updateLastInjectionTime(calendar.timeInMillis, this)
                        updateLastInjectionDisplay()
                        notifyWidgetUpdate()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun notifyWidgetUpdate() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetComponent = ComponentName(this, InsulinCalcWidget::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        if (widgetIds.isNotEmpty()) {
            val intent = Intent(this, InsulinCalcWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            sendBroadcast(intent)
        }
    }
}
