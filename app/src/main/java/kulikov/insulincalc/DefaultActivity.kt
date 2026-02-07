package kulikov.insulincalc

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

class DefaultActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView)  { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvFindTutorial = findViewById<AppCompatTextView>(R.id.tvFindTutorial)
        val llClickToAdd = findViewById<LinearLayout>(R.id.llClickToEnable)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvFindTutorial.isVisible = false
            val appWidgetManager: AppWidgetManager = getSystemService(AppWidgetManager::class.java)
            val widgetProvider = ComponentName(this, InsulinCalcWidget::class.java)

            val pinnedWidgetCallbackIntent = Intent(this, InsulinCalcWidget::class.java)

            val successCallback: PendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                pinnedWidgetCallbackIntent,
                PendingIntent.FLAG_MUTABLE
            )
            val btnAddToHomeScreen = findViewById<AppCompatButton>(R.id.btnAddToHomeScreen)
            btnAddToHomeScreen.setOnClickListener {
                if (!appWidgetManager.requestPinAppWidget(widgetProvider, null, successCallback)) {
                    Toast.makeText(this, getString(R.string.add_widget_error), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            llClickToAdd.isVisible = false
        }

        findViewById<AppCompatButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<AppCompatButton>(R.id.btnTutorial).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW,
                "https://www.youtube.com/watch?v=uQ4XJtGcHpo".toUri())
            try {
                startActivity(browserIntent)
            } catch (_: Throwable) {
                Toast.makeText(this, "Cannot find a browser app on device", Toast.LENGTH_SHORT).show()
            }
        }
    }
}