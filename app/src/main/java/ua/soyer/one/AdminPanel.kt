package ua.soyer.one

import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private const val SOYER_API = "https://soyer-exchange-api.onrender.com"
private val soyerCookies = CookieManager(null, CookiePolicy.ACCEPT_ALL)
@Volatile private var soyerAdminToken: String = ""
@Volatile var soyerUserToken: String = ""

fun installSoyerCookieManager() {
    CookieHandler.setDefault(soyerCookies)
}

data class SoyerSettings(
    val rateUsdtToUah: Double = 46.50,
    val rateUahToUsdt: Double = 47.70,
    val minimumUsdt: Double = 10.0,
    val updatedAt: String = "13.11.2026",
    val managerUrl: String = "https://t.me/soyermanager",
    val exchangeUrl: String = "https://t.me/soyerexchangecom/3",
    val projectsUrl: String = "https://t.me/soyer_pr",
    val heroTitle: String = "Вітаємо у SOYER ONE",
    val heroText: String = "Ваш простір для зручних розрахунків та інформації про курси.",
    val aboutText: String = "Ми — команда SOYER EXCHANGE.",
    val partnershipText: String = "Відкриті до співпраці та нових партнерств."
) {
    companion object {
        fun from(json: JSONObject) = SoyerSettings(
            rateUsdtToUah = json.optDouble("rateUsdtToUah", 46.50),
            rateUahToUsdt = json.optDouble("rateUahToUsdt", 47.70),
            minimumUsdt = json.optDouble("minimumUsdt", 10.0),
            updatedAt = json.optString("updatedAt", "13.11.2026"),
            managerUrl = json.optString("managerUrl", "https://t.me/soyermanager"),
            exchangeUrl = json.optString("exchangeUrl", "https://t.me/soyerexchangecom/3"),
            projectsUrl = json.optString("projectsUrl", "https://t.me/soyer_pr"),
            heroTitle = json.optString("heroTitle", "Вітаємо у SOYER ONE"),
            heroText = json.optString("heroText", "Ваш простір для зручних розрахунків та інформації про курси."),
            aboutText = json.optString("aboutText", "Ми — команда SOYER EXCHANGE."),
            partnershipText = json.optString("partnershipText", "Відкриті до співпраці та нових партнерств.")
        )
    }
}

fun formatSoyerRate(value: Double): String =
    String.format(Locale.US, "%.2f", value).replace('.', ',')

suspend fun soyerApiRequest(path: String, method: String = "GET", body: JSONObject? = null): JSONObject =
    withContext(Dispatchers.IO) {
        val connection = URL(SOYER_API + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 65000
            connection.readTimeout = 65000
            connection.setRequestProperty("Accept", "application/json")
            if (path.startsWith("/api/admin/") && soyerAdminToken.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $soyerAdminToken")
            }
            if ((path.startsWith("/api/auth/") || path.startsWith("/api/requests") || path.startsWith("/api/admin/")) && soyerUserToken.isNotBlank() && soyerAdminToken.isBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $soyerUserToken")
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = if (response.isBlank()) JSONObject() else JSONObject(response)
            if (status !in 200..299) {
                throw IllegalStateException(json.optString("error").ifBlank { "Помилка сервера: HTTP " + status })
            }
            json
        } finally {
            connection.disconnect()
        }
    }

@Composable
fun AdminPanel(
    modifier: Modifier = Modifier,
    current: SoyerSettings,
    onSettingsUpdated: (SoyerSettings) -> Unit
) {
    val scope = rememberCoroutineScope()
    var authenticated by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var rateSell by remember { mutableStateOf(formatSoyerRate(current.rateUsdtToUah)) }
    var rateBuy by remember { mutableStateOf(formatSoyerRate(current.rateUahToUsdt)) }
    var minimum by remember { mutableStateOf(formatSoyerRate(current.minimumUsdt)) }
    var updatedAt by remember { mutableStateOf(current.updatedAt) }
    var heroTitle by remember { mutableStateOf(current.heroTitle) }
    var heroText by remember { mutableStateOf(current.heroText) }
    var aboutText by remember { mutableStateOf(current.aboutText) }
    var partnershipText by remember { mutableStateOf(current.partnershipText) }
    var managerUrl by remember { mutableStateOf(current.managerUrl) }
    var exchangeUrl by remember { mutableStateOf(current.exchangeUrl) }
    var projectsUrl by remember { mutableStateOf(current.projectsUrl) }
    var rawSettings by remember { mutableStateOf(JSONObject()) }
    var requests by remember { mutableStateOf(emptyList<JSONObject>()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    suspend fun loadRequests() {
        val response = soyerApiRequest("/api/admin/requests")
        val array = response.optJSONArray("requests") ?: org.json.JSONArray()
        requests = (0 until array.length()).mapNotNull { array.optJSONObject(it) }
    }

    suspend fun loadSettings() {
        val json = soyerApiRequest("/api/settings")
        rawSettings = JSONObject(json.toString())
        val value = SoyerSettings.from(json)
        rateSell = formatSoyerRate(value.rateUsdtToUah)
        rateBuy = formatSoyerRate(value.rateUahToUsdt)
        minimum = formatSoyerRate(value.minimumUsdt)
        updatedAt = value.updatedAt
        heroTitle = value.heroTitle
        heroText = value.heroText
        aboutText = value.aboutText
        partnershipText = value.partnershipText
        managerUrl = value.managerUrl
        exchangeUrl = value.exchangeUrl
        projectsUrl = value.projectsUrl
        onSettingsUpdated(value)
    }

    LaunchedEffect(Unit) {
        try {
            soyerApiRequest("/api/admin/me")
            authenticated = true
            loadSettings()
            loadRequests()
        } catch (_: Exception) { }
    }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Адмін-панель", color = Color(0xFFF5F7FB), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "Керування SOYER EXCHANGE. Зміни публікуються на сервері та синхронізуються для користувачів.",
            color = Color(0xFF8D98AA), fontSize = 13.sp, lineHeight = 19.sp
        )

        if (!authenticated) {
            Column(
                Modifier.fillMaxWidth().background(Color(0xFF111722), RoundedCornerShape(18.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Доступ обмежено", color = Color(0xFFF5F7FB), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Адмін-панель доступна лише Telegram-акаунту, який доданий до ADMIN_IDS на сервері.", color = Color(0xFF8D98AA), fontSize = 13.sp, lineHeight = 19.sp)
            }
        } else {
            Text("ЗАЯВКИ КЛІЄНТІВ", color = Color(0xFF8D98AA), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            OutlinedButton(onClick = { scope.launch { try { loadRequests() } catch (e: Exception) { error = e.message ?: "Помилка завантаження заявок." } } }, modifier = Modifier.fillMaxWidth()) { Text("Оновити заявки") }
            if (requests.isEmpty()) Text("Заявок поки немає.", color = Color(0xFF8D98AA), fontSize = 12.sp)
            requests.forEach { item ->
                val id = item.optString("id")
                Column(Modifier.fillMaxWidth().background(Color(0xFF111722), RoundedCornerShape(16.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("#" + id + " · " + item.optString("firstName").ifBlank { "Клієнт" }, color = Color(0xFFF5F7FB), fontWeight = FontWeight.Bold)
                    Text(item.optString("amountFrom") + " " + (if (item.optString("direction") == "USDT_TO_UAH") "USDT → UAH " else "UAH → USDT ") + item.optString("amountTo"), color = Color(0xFF6C8CFF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Telegram ID: " + item.optString("telegramUserId") + " · Статус: " + item.optString("status"), color = Color(0xFF8D98AA), fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("processing" to "В обробці", "completed" to "Готово", "cancelled" to "Скасувати").forEach { (nextStatus, label) ->
                            TextButton(onClick = { scope.launch { try { soyerApiRequest("/api/admin/requests/" + id, "PATCH", JSONObject().put("status", nextStatus)); loadRequests() } catch (e: Exception) { error = e.message ?: "Помилка статусу." } } }) { Text(label, fontSize = 10.sp) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("НАЛАШТУВАННЯ КУРСУ", color = Color(0xFF8D98AA), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            AdminInput("USDT → UAH", rateSell, { rateSell = it }, KeyboardType.Decimal)
            AdminInput("UAH → USDT", rateBuy, { rateBuy = it }, KeyboardType.Decimal)
            AdminInput("Мінімум (USDT)", minimum, { minimum = it }, KeyboardType.Decimal)
            AdminInput("Дата оновлення", updatedAt, { updatedAt = it })
            AdminInput("Заголовок", heroTitle, { heroTitle = it })
            AdminInput("Опис головного екрана", heroText, { heroText = it }, multiline = true)
            AdminInput("Про компанію", aboutText, { aboutText = it }, multiline = true)
            AdminInput("Партнерство", partnershipText, { partnershipText = it }, multiline = true)
            AdminInput("Посилання менеджера", managerUrl, { managerUrl = it })
            AdminInput("Канал обміну", exchangeUrl, { exchangeUrl = it })
            AdminInput("Проєкти", projectsUrl, { projectsUrl = it })

            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        error = ""
                        message = ""
                        try {
                            val sell = rateSell.replace(" ", "").replace(',', '.').toDoubleOrNull()
                                ?: throw IllegalArgumentException("Перевірте курс USDT → UAH.")
                            val buy = rateBuy.replace(" ", "").replace(',', '.').toDoubleOrNull()
                                ?: throw IllegalArgumentException("Перевірте курс UAH → USDT.")
                            val min = minimum.replace(" ", "").replace(',', '.').toDoubleOrNull()
                                ?: throw IllegalArgumentException("Перевірте мінімальну суму.")
                            val payload = JSONObject(rawSettings.toString())
                                .put("rateUsdtToUah", sell)
                                .put("rateUahToUsdt", buy)
                                .put("minimumUsdt", min)
                                .put("updatedAt", updatedAt)
                                .put("heroTitle", heroTitle)
                                .put("heroText", heroText)
                                .put("aboutText", aboutText)
                                .put("partnershipText", partnershipText)
                                .put("managerUrl", managerUrl)
                                .put("exchangeUrl", exchangeUrl)
                                .put("projectsUrl", projectsUrl)
                            val response = soyerApiRequest("/api/admin/settings", "PUT", payload)
                            val saved = response.optJSONObject("settings") ?: payload
                            rawSettings = JSONObject(saved.toString())
                            val value = SoyerSettings.from(saved)
                            onSettingsUpdated(value)
                            message = "Збережено на сервері. Зміни побачать усі користувачі."
                        } catch (e: Exception) {
                            error = e.message ?: "Не вдалося зберегти зміни."
                        } finally { busy = false }
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C8CFF), contentColor = Color(0xFF080A0F))
            ) { Text(if (busy) "Збереження..." else "Зберегти зміни", fontWeight = FontWeight.Bold) }

            OutlinedButton(
                onClick = {
                    soyerAdminToken = ""
                    authenticated = false
                    message = "Адмін-панель закрито."
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Закрити адмін-панель") }
        }
        if (error.isNotBlank()) Text(error, color = Color(0xFFFF9A9A), fontSize = 12.sp)
        if (message.isNotBlank()) Text(message, color = Color(0xFF6C8CFF), fontSize = 12.sp)
        Text("Права адміністратора перевіряються сервером за Telegram ID.", color = Color(0xFF8D98AA), fontSize = 11.sp)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun AdminInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text,
    multiline: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard)
    )
}
