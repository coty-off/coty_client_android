package coty.band.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CotyRed    = Color(0xFFE53935)
private val CotyRedDark = Color(0xFFB71C1C)
private val CotyGray   = Color(0xFFF5F5F5)

private val LightColors = lightColorScheme(
    primary         = CotyRed,
    onPrimary       = Color.White,
    primaryContainer = CotyRedDark,
    secondary       = Color(0xFF616161),
    background      = Color.White,
    surface         = CotyGray,
    error           = Color(0xFFB00020)
)

@Composable
fun CotyappTheme(content: @Composable () -> Unit){
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}