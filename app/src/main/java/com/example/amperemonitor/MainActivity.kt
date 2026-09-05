package com.example.amperemonitor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    AmpereMonitorScreen()
                }
            }
        }
    }
}

@Composable
fun AmpereMonitorScreen() {
    val context = LocalContext.current

    val currentAmperes = remember { mutableFloatStateOf(0f) }
    val voltage = remember { mutableFloatStateOf(0f) }
    val temperature = remember { mutableFloatStateOf(0f) }
    val batteryLevel = remember { mutableStateOf("0%") }
    val chargingStatus = remember { mutableStateOf("Discharging") }

    LaunchedEffect(Unit) {
        while (true) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)

            // Current in mA
            val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            currentAmperes.floatValue = currentNow / 1000f

            // Voltage, Temp, Level
            batteryStatus?.let {
                val volt = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000f
                val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                voltage.floatValue = volt
                temperature.floatValue = temp
                batteryLevel.value = "${((level / scale.toFloat()) * 100).toInt()}%"

                chargingStatus.value = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full"
                    else -> "Not Charging"
                }
            }

            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ampere Monitor",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Ampere Display Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentAmperes.floatValue.toInt()} mA",
                color = if (currentAmperes.floatValue >= 0) Color(0xFF4CAF50) else Color(0xFFE53935),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = chargingStatus.value,
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Battery Info Cards
        InfoRow(label = "Level", value = batteryLevel.value)
        InfoRow(label = "Voltage", value = "${voltage.floatValue} V")
        InfoRow(label = "Temperature", value = "${temperature.floatValue} °C")
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
