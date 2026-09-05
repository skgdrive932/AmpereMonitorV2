package com.example.amperemonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

data class BatteryInfo(
    val level: Int = 0,
    val temperatureC: Float = 0f,
    val voltageV: Float = 0f,
    val status: String = "Unknown",
    val plugged: String = "Not plugged",
    val health: String = "Unknown",
    val technology: String = "Unknown"
)

class MainActivity : ComponentActivity() {

    private val batteryManager by lazy {
        getSystemService(BATTERY_SERVICE) as BatteryManager
    }

    private var batteryInfo by mutableStateOf(BatteryInfo())
    private var currentMa by mutableFloatStateOf(Float.NaN)
    private var minMa by mutableFloatStateOf(Float.NaN)
    private var maxMa by mutableFloatStateOf(Float.NaN)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            batteryInfo = parseBatteryIntent(intent)
            readCurrent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sticky = registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        if (sticky != null) {
            batteryInfo = parseBatteryIntent(sticky)
        }

        setContent {
            AmpereScreen(
                batteryInfo = batteryInfo,
                currentMa = currentMa,
                minMa = minMa,
                maxMa = maxMa
            )

            LaunchedEffect(Unit) {
                while (true) {
                    readCurrent()
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
    }

    private fun parseBatteryIntent(intent: Intent): BatteryInfo {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)

        return BatteryInfo(
            level = if (scale > 0) level * 100 / scale else 0,
            temperatureC = temperature,
            voltageV = voltage,
            status = statusText(status),
            plugged = pluggedText(plugged),
            health = healthText(health),
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        )
    }

    private fun readCurrent() {
        val microAmps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } else {
            Int.MIN_VALUE
        }

        if (microAmps == Int.MIN_VALUE || microAmps == 0) {
            currentMa = Float.NaN
            return
        }

        val ma = abs(microAmps) / 1000f
        currentMa = ma

        if (minMa.isNaN() || ma < minMa) minMa = ma
        if (maxMa.isNaN() || ma > maxMa) maxMa = ma
    }

    private fun statusText(status: Int) = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Unknown"
    }

    private fun pluggedText(value: Int) = when (value) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC charger"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "Not plugged"
    }

    private fun healthText(value: Int) = when (value) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }
}

@androidx.compose.runtime.Composable
fun AmpereScreen(
    batteryInfo: BatteryInfo,
    currentMa: Float,
    minMa: Float,
    maxMa: Float
) {
    val teal = Color(0xFF00BFAE)
    val header = Color(0xFF455A64)

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(header)
                    .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AmpereMonitor",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Share, "Share", tint = Color.White)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .width(52.dp)
                                .height(8.dp)
                                .background(
                                    if (index < 2) teal else Color(0xFF60737B),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = if (currentMa.isNaN()) "Unavailable" else
                        String.format(Locale.US, "%.0f mA", currentMa),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = teal,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "min: " + if (minMa.isNaN()) "—" else
                            String.format(Locale.US, "%.0f mA", minMa),
                        color = teal,
                        fontSize = 20.sp
                    )
                    Text(
                        "max: " + if (maxMa.isNaN()) "—" else
                            String.format(Locale.US, "%.0f mA", maxMa),
                        color = teal,
                        fontSize = 20.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BatteryRow("Status:", batteryInfo.status, teal)
                BatteryRow("Plugged:", batteryInfo.plugged, teal)
                BatteryRow("Level:", "${batteryInfo.level}%", teal)
                BatteryRow("Health:", batteryInfo.health, teal)
                BatteryRow("Technology:", batteryInfo.technology, teal)

                BatteryRow(
                    "Temperature:",
                    String.format(Locale.US, "%.1f °C", batteryInfo.temperatureC),
                    teal
                )

                BatteryRow(
                    "Voltage:",
                    String.format(Locale.US, "%.2f V", batteryInfo.voltageV),
                    teal
                )

                Spacer(Modifier.height(22.dp))

                BatteryRow("Manufacturer:", Build.MANUFACTURER, teal)
                BatteryRow("Model:", Build.MODEL, teal)
                BatteryRow("Android version:", Build.VERSION.RELEASE, teal)
                BatteryRow("Build ID:", Build.ID, teal)
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun BatteryRow(title: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text(value, color = color, fontSize = 18.sp)
    }
}
