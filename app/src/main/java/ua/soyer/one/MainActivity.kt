package ua.soyer.one

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import kotlinx.coroutines.launch

private val Background = Color(0xFF080A0F)
private val Surface = Color(0xFF111722)
private val SurfaceLight = Color(0xFF151E2B)
private val Green = Color(0xFF6C8CFF)
private val GreenDark = Color(0xFF252B55)
private val Violet = Color(0xFF9C79FF)
private val AccentGreen = Color(0xFF42D6A0)
private val MainText = Color(0xFFF5F7FB)
private val Muted = Color(0xFF8D98AA)
private val Border = Color(0xFF29334A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSoyerCookieManager()
        setContent { SoyerRoot() }
    }
}


@Composable
fun SoyerRoot() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("soyer_one", android.content.Context.MODE_PRIVATE) }
    val authScope = rememberCoroutineScope()
    var token by remember { mutableStateOf(preferences.getString("telegram_token", "").orEmpty()) }
    var firstName by remember { mutableStateOf(preferences.getString("telegram_first_name", "").orEmpty()) }
    var username by remember { mutableStateOf(preferences.getString("telegram_username", "").orEmpty()) }
    var telegramId by remember { mutableStateOf(preferences.getString("telegram_id", "").orEmpty()) }
    var checking by remember { mutableStateOf(token.isNotBlank()) }

    LaunchedEffect(Unit) {
        if (token.isNotBlank()) {
            soyerUserToken = token
            try {
                val profile = soyerApiRequest("/api/auth/me").optJSONObject("user") ?: throw IllegalStateException("Invalid profile")
                firstName = profile.optString("firstName")
                username = profile.optString("username")
                telegramId = profile.optString("id")
            } catch (_: Exception) {
                token = ""
                soyerUserToken = ""
                preferences.edit().remove("telegram_token").remove("telegram_first_name")
                    .remove("telegram_username").remove("telegram_id").apply()
            }
        }
        checking = false
    }

    when {
        checking -> Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Green)
        }
        token.isBlank() -> TelegramAuthScreen { result ->
            val newToken = result.optString("token")
            val profile = result.optJSONObject("user")
            if (newToken.isNotBlank() && profile != null) {
                token = newToken
                soyerUserToken = newToken
                firstName = profile.optString("firstName")
                username = profile.optString("username")
                telegramId = profile.optString("id")
                preferences.edit().putString("telegram_token", newToken)
                    .putString("telegram_first_name", firstName)
                    .putString("telegram_username", username)
                    .putString("telegram_id", telegramId).apply()
            }
        }
        else -> SoyerApp(
            telegramName = firstName,
            telegramUsername = username,
            telegramId = telegramId,
            onLogout = {
                authScope.launch {
                    try { soyerApiRequest("/api/auth/logout", "POST") } catch (_: Exception) { }
                    token = ""
                    soyerUserToken = ""
                    preferences.edit().remove("telegram_token").remove("telegram_first_name")
                        .remove("telegram_username").remove("telegram_id").apply()
                }
            }
        )
    }
}

@Composable
fun SoyerApp(telegramName: String, telegramUsername: String, telegramId: String, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var settings by remember { mutableStateOf(SoyerSettings()) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        while (true) {
            try { settings = SoyerSettings.from(soyerApiRequest("/api/settings")) } catch (_: Exception) { }
            kotlinx.coroutines.delay(60_000)
        }
    }
    val preferences = remember { context.getSharedPreferences("soyer_one", android.content.Context.MODE_PRIVATE) }
    val historyItems = remember {
        mutableStateListOf<String>().apply {
            addAll(preferences.getString("history", "").orEmpty().lines().filter { it.isNotBlank() })
        }
    }
    fun saveHistory(entry: String) {
        historyItems.add(0, entry)
        if (historyItems.size > 50) historyItems.removeAt(historyItems.lastIndex)
        preferences.edit().putString("history", historyItems.joinToString("\n")).apply()
    }
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = Surface,
            primary = Green,
            onPrimary = Background,
            onBackground = MainText,
            onSurface = MainText
        )
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF0D121B),
                    contentColor = Muted
                ) {
                    val items = listOf(
                        Triple("Головна", Icons.Filled.Home, 0),
                        Triple("Калькулятор", Icons.Filled.Calculate, 1),
                        Triple("Історія", Icons.Filled.History, 2),
                        Triple("Профіль", Icons.Filled.Person, 3)
                    )
                    items.forEach { (label, icon, index) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Green,
                                selectedTextColor = Green,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted,
                                indicatorColor = GreenDark
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> HomeScreen(Modifier.padding(padding), onCalculate = { selectedTab = 1 }, onAdminOpen = { selectedTab = 4 }, settings = settings)
                1 -> CalculatorScreen(Modifier.padding(padding), onSave = ::saveHistory, settings = settings)
                2 -> HistoryScreen(Modifier.padding(padding), historyItems)
                3 -> ProfileScreen(Modifier.padding(padding), telegramName, telegramUsername, telegramId, onLogout)
                else -> AdminPanel(Modifier.padding(padding), current = settings, onSettingsUpdated = { settings = it })
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onCalculate: () -> Unit, onAdminOpen: () -> Unit = {}, settings: SoyerSettings = SoyerSettings()) {
    val context = LocalContext.current
    var logoTaps by remember { mutableIntStateOf(0) }
    fun openTelegram(username: String) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$username"))) }
    Column(
        modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1220), Background, Background, Color(0xFF0A0E16))))
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(GreenDark)
                    .clickable {
                        logoTaps += 1
                        if (logoTaps >= 7) {
                            logoTaps = 0
                            onAdminOpen()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("S", color = Green, fontSize = 25.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("SOYER", color = MainText, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp)
                Text("ONE", color = Green, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 4.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.clip(CircleShape).background(Surface).padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(AccentGreen))
                    Spacer(Modifier.width(7.dp))
                    Text("ONLINE", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(settings.heroTitle, color = MainText, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 33.sp)
            Text(settings.heroText, color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(SurfaceLight, Color(0xFF101827), Color(0xFF17182B))))
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("АКТУАЛЬНИЙ КУРС ОБМІНУ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("USDT → UAH", color = Muted, fontSize = 13.sp)
                    Text("${formatSoyerRate(settings.rateUsdtToUah)} ₴", color = MainText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(42.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GreenDark, Violet.copy(alpha = 0.45f)))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.SwapVert, contentDescription = null, tint = Green)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("UAH → USDT", color = Muted, fontSize = 13.sp)
                    Text("${formatSoyerRate(settings.rateUahToUsdt)} ₴", color = MainText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("Мінімум ${formatSoyerRate(settings.minimumUsdt)} USDT · Оновлено: ${settings.updatedAt}. Орієнтовні значення.", color = Muted, fontSize = 10.sp)
        }

        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Background)
        ) {
            Text("Розрахувати обмін", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.SwapVert, contentDescription = null)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ПРО SOYER EXCHANGE", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text(settings.aboutText, color = MainText, fontSize = 13.sp, lineHeight = 19.sp)
            FeatureRow("Зручність", "USDT через блокчейн-мережі або UID бірж")
            FeatureRow("Особисті рахунки", "Перекази з особистих рахунків команди")
            FeatureRow("Прозорі умови", "Курс та умови погоджуються перед угодою")
            FeatureRow("Гнучкість", "Можливість P2P за попереднім погодженням")
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("ПАРТНЕРСТВО", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text(settings.partnershipText, color = MainText, fontSize = 13.sp, lineHeight = 19.sp)
            Text("Індивідуальні умови та довгострокова співпраця.", color = Muted, fontSize = 12.sp)
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settings.managerUrl))) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Обговорити партнерство")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("АКЦІЇ ТА БОНУСИ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            FeatureRow("Ексклюзивні курси", "Спеціальні пропозиції для клієнтів")
            FeatureRow("Розіграші", "Стежте за оголошеннями в Telegram")
            FeatureRow("Реферальна програма", "Винагороди за рекомендації друзям")
            OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settings.exchangeUrl))) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Стежити за оновленнями")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ЗВ’ЯЗОК", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settings.managerUrl))) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Background)) {
                Text("Написати менеджеру", fontWeight = FontWeight.Bold)
            }
            Text("Проєкти: @soyer_pr", color = Muted, fontSize = 12.sp, modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settings.projectsUrl))) })
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun FeatureRow(title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(GreenDark), contentAlignment = Alignment.Center) {
            Text("✓", color = Green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier, onSave: (String) -> Unit = {}, settings: SoyerSettings = SoyerSettings()) {
    val requestScope = rememberCoroutineScope()
    var requestBusy by remember { mutableStateOf(false) }
    var requestMessage by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(true) }
    var give by remember { mutableStateOf("100") }
    var receive by remember { mutableStateOf("4660") }
    val rate = if (direction) settings.rateUsdtToUah else 1.0 / settings.rateUahToUsdt
    val formatter = remember { DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US)) }
    fun formatAmount(value: Double): String = formatter.format(value).replace(",", " ").replace(".", ",")
    val giveAmount = give.filterNot { it.isWhitespace() }.replace(',', '.').toDoubleOrNull()
    val receiveAmount = receive.filterNot { it.isWhitespace() }.replace(',', '.').toDoubleOrNull()
    val minimumMet = if (direction) (giveAmount ?: 0.0) >= settings.minimumUsdt else (giveAmount ?: 0.0) >= settings.minimumUsdt * settings.rateUahToUsdt

    fun updateGive(value: String) {
        give = value
        val amount = value.filterNot { it.isWhitespace() }.replace(',', '.').toDoubleOrNull()
        receive = amount?.let { formatAmount(it * rate) } ?: ""
    }
    fun updateReceive(value: String) {
        receive = value
        val amount = value.filterNot { it.isWhitespace() }.replace(',', '.').toDoubleOrNull()
        give = amount?.let { formatAmount(it / rate) } ?: ""
    }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(8.dp))
        Text("Калькулятор", color = MainText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("Оберіть напрямок та введіть суму.", color = Muted, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface).padding(4.dp)) {
            DirectionChip("USDT → UAH", direction, Modifier.weight(1f)) {
                direction = true
                updateGive(give)
            }
            DirectionChip("UAH → USDT", !direction, Modifier.weight(1f)) {
                direction = false
                updateGive(give)
            }
        }
        AmountCard("ВІДДАЮ", if (direction) "USDT" else "UAH", give, { updateGive(it) })
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(GreenDark), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.SwapVert, contentDescription = null, tint = Green)
            }
        }
        AmountCard("ОТРИМУЮ", if (direction) "UAH" else "USDT", receive, { updateReceive(it) })
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ІНДИКАТИВНИЙ КУРС", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(if (direction) "1 USDT = ${formatSoyerRate(settings.rateUsdtToUah)} UAH" else "1 USDT = ${formatSoyerRate(settings.rateUahToUsdt)} UAH", color = Green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Остаточний курс та умови підтверджуються менеджером перед обміном.", color = Muted, fontSize = 11.sp)
        }
        Button(
            onClick = {
                val from = if (direction) "USDT" else "UAH"
                val to = if (direction) "UAH" else "USDT"
                val timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("uk", "UA")).format(Date())
                onSave("$give $from → $receive $to · $timestamp")
            },
            enabled = give.isNotBlank() && receive.isNotBlank() && minimumMet,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Background)
        ) {
            Text("Зберегти розрахунок", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = {
                val fromAmount = giveAmount ?: 0.0
                val toAmount = receiveAmount ?: 0.0
                requestScope.launch {
                    requestBusy = true
                    requestMessage = ""
                    try {
                        val payload = JSONObject()
                            .put("direction", if (direction) "USDT_TO_UAH" else "UAH_TO_USDT")
                            .put("amountFrom", fromAmount)
                            .put("amountTo", toAmount)
                        soyerApiRequest("/api/requests", "POST", payload)
                        requestMessage = "Заявку створено. Менеджер зв’яжеться з вами в Telegram."
                    } catch (e: Exception) {
                        requestMessage = e.message ?: "Не вдалося створити заявку."
                    } finally { requestBusy = false }
                }
            },
            enabled = give.isNotBlank() && receive.isNotBlank() && minimumMet && !requestBusy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Background)
        ) { Text(if (requestBusy) "Надсилання..." else "Створити заявку", fontWeight = FontWeight.Bold) }
        if (requestMessage.isNotBlank()) Text(requestMessage, color = if (requestMessage.startsWith("Заявку")) AccentGreen else Color(0xFFFF9A9A), fontSize = 12.sp, lineHeight = 18.sp)
        if (!minimumMet) {
            Text("Мінімальна сума — ${formatSoyerRate(settings.minimumUsdt)} USDT. Змініть суму, щоб зберегти розрахунок.", color = Color(0xFFFFC36B), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier, items: List<String>) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Історія", color = MainText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("Збережені розрахунки на цьому пристрої.", color = Muted, fontSize = 14.sp)
        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Поки що немає збережених розрахунків.", color = Muted, fontSize = 13.sp)
            }
        } else {
            items.forEachIndexed { index, item ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(15.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(GreenDark), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Green, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(item, color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DirectionChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(11.dp))
            .background(if (selected) GreenDark else Color.Transparent)
            .clickable(onClick = onClick).padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Green else Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AmountCard(label: String, currency: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Surface).border(1.dp, Border, RoundedCornerShape(20.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            Text(currency, color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' || c == ',' || c == ' ' }) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = androidx.compose.ui.text.TextStyle(color = MainText, fontSize = 27.sp, fontWeight = FontWeight.Bold),
            decorationBox = { inner ->
                if (value.isEmpty()) Text("0,00", color = Muted, fontSize = 27.sp)
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    telegramName: String,
    telegramUsername: String,
    telegramId: String,
    onLogout: () -> Unit
) {
    Column(
        modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Профіль", color = MainText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface)
                .border(1.dp, Border, RoundedCornerShape(20.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("TELEGRAM АККАУНТ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(telegramName.ifBlank { "Користувач Telegram" }, color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (telegramUsername.isNotBlank()) Text("@$telegramUsername", color = Green, fontSize = 14.sp)
            Text("ID: $telegramId", color = Muted, fontSize = 12.sp)
            Text("Авторизація активна", color = AccentGreen, fontSize = 12.sp)
        }
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Вийти з акаунта", color = MainText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoScreen(modifier: Modifier = Modifier, title: String, description: String) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(12.dp))
        Text(title, color = MainText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(description, color = Muted, fontSize = 14.sp, lineHeight = 22.sp)
        if (title == "Профіль") {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = Green)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Підтримка", color = MainText, fontWeight = FontWeight.SemiBold)
                    Text("Розділ буде підключено в наступній версії.", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}
