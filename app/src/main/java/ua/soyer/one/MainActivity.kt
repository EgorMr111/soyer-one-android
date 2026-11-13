package ua.soyer.one

import android.os.Bundle
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
import androidx.compose.material.icons.filled.ArrowDownUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

private val Background = Color(0xFF080D0B)
private val Surface = Color(0xFF111A16)
private val SurfaceLight = Color(0xFF18251E)
private val Green = Color(0xFF20D887)
private val GreenDark = Color(0xFF123D2B)
private val MainText = Color(0xFFF2F7F3)
private val Muted = Color(0xFF91A198)
private val Border = Color(0xFF26372E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SoyerApp() }
    }
}

@Composable
fun SoyerApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
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
                    containerColor = Color(0xFF0D1410),
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
                0 -> HomeScreen(Modifier.padding(padding), onCalculate = { selectedTab = 1 })
                1 -> CalculatorScreen(Modifier.padding(padding))
                2 -> InfoScreen(Modifier.padding(padding), "Історія", "Тут відображатимуться ваші останні розрахунки.")
                else -> InfoScreen(Modifier.padding(padding), "Профіль", "Налаштування профілю та підтримка SOYER ONE.")
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onCalculate: () -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(GreenDark), contentAlignment = Alignment.Center) {
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
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Green))
                    Spacer(Modifier.width(7.dp))
                    Text("ОНЛАЙН", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Вітаємо у", color = Muted, fontSize = 16.sp)
            Text("SOYER ONE", color = MainText, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("Ваш простір для зручних розрахунків та інформації про курси.", color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(SurfaceLight)
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("КУРСИ ДЛЯ ПРИКЛАДУ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("USDT → UAH", color = Muted, fontSize = 13.sp)
                    Text("46,60 ₴", color = MainText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(42.dp).clip(CircleShape).background(GreenDark), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ArrowDownUp, contentDescription = null, tint = Green)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("UAH → USDT", color = Muted, fontSize = 13.sp)
                    Text("47,70 ₴", color = MainText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("Демонстраційні значення. Не є актуальною фінансовою пропозицією.", color = Muted, fontSize = 10.sp)
        }

        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Background)
        ) {
            Text("Відкрити калькулятор", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowDownUp, contentDescription = null)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ПЕРЕВАГИ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            FeatureRow("Зручний інтерфейс", "Усі основні розділи в одному місці")
            FeatureRow("Швидкий розрахунок", "Змінюйте будь-яке поле калькулятора")
            FeatureRow("Підтримка", "Інформація для зв’язку з командою")
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
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var direction by remember { mutableStateOf(true) }
    var give by remember { mutableStateOf("100") }
    var receive by remember { mutableStateOf("4660") }
    val rate = if (direction) 46.60 else 1.0 / 47.70
    val formatter = remember { DecimalFormat("#,##0.00") }

    fun updateGive(value: String) {
        give = value
        val amount = value.replace(',', '.').toDoubleOrNull()
        receive = amount?.let { formatter.format(it * rate).replace(",", " ").replace(".", ",") } ?: ""
    }
    fun updateReceive(value: String) {
        receive = value
        val amount = value.replace(" ", "").replace(',', '.').toDoubleOrNull()
        give = amount?.let { formatter.format(it / rate).replace(",", " ").replace(".", ",") } ?: ""
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
                Icon(Icons.Filled.ArrowDownUp, contentDescription = null, tint = Green)
            }
        }
        AmountCard("ОТРИМУЮ", if (direction) "UAH" else "USDT", receive, { updateReceive(it) })
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ІНДИКАТИВНИЙ КУРС", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(if (direction) "1 USDT = 46,60 UAH" else "1 USDT = 47,70 UAH", color = Green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Тестові значення для демонстрації інтерфейсу.", color = Muted, fontSize = 11.sp)
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
