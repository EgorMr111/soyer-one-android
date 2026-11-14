package ua.soyer.one

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

private const val TELEGRAM_LOGIN_URL = "https://soyer-exchange-api.onrender.com/telegram-login"

private class SoyerTelegramBridge(
    private val activity: Activity,
    private val onAuthenticated: (JSONObject) -> Unit
) {
    @JavascriptInterface
    fun onAuth(response: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                onAuthenticated(JSONObject(response))
            } catch (_: Exception) {
                // Ignore malformed responses from the login page.
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TelegramAuthScreen(onAuthenticated: (JSONObject) -> Unit) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    Column(
        Modifier.fillMaxSize().background(Color(0xFF080A0F)).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SOYER", color = Color(0xFFF5F7FB), fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Text("ONE", color = Color(0xFF6C8CFF), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 5.sp)
        Spacer(Modifier.height(12.dp))
        Text("Увійдіть, щоб продовжити", color = Color(0xFFF5F7FB), fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("Безпечна авторизація через ваш Telegram-акаунт.", color = Color(0xFF8D98AA), fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.fillMaxWidth().height(270.dp).clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF111722)).border(1.dp, Color(0xFF29334A), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    WebView(viewContext).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        webViewClient = WebViewClient()
                        webChromeClient = WebChromeClient()
                        addJavascriptInterface(
                            SoyerTelegramBridge(context as Activity, onAuthenticated),
                            "SoyerTelegramBridge"
                        )
                        loadUrl(TELEGRAM_LOGIN_URL)
                    }
                },
                update = { }
            )
            if (loading) {
                CircularProgressIndicator(color = Color(0xFF6C8CFF), modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("SOYER ONE не отримує ваш пароль Telegram.", color = Color(0xFF8D98AA), fontSize = 11.sp)
    }
}
