package com.hp.aiassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.net.URL
import org.json.JSONObject

private val Purple = Color(0xFF8172FF)
private val DarkBg = Color(0xFF10111A)
private val DarkCard = Color(0xFF1B1D2B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HPAssistantApp() }
    }
}

@Composable
fun HPAssistantApp() {
    var dark by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf("welcome") }
    var signedIn by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var updateMessage by remember { mutableStateOf("") }
    val bg = if (dark) DarkBg else Color(0xFFF5F5FC)
    val card = if (dark) DarkCard else Color.White
    val text = if (dark) Color.White else Color(0xFF171725)
    val muted = if (dark) Color(0xFFB4B5C8) else Color(0xFF66677B)

    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Purple, background = bg, surface = card) else lightColorScheme(primary = Purple, background = bg, surface = card)) {
        Surface(modifier = Modifier.fillMaxSize(), color = bg) {
            when (page) {
                "welcome", "login", "signup" -> AuthScreen(page, { page = it }, { dark = !dark }, dark, text, muted, card) { mail -> email = mail; signedIn = true; page = "chat" }
                "chat" -> ChatScreen(email, dark, text, muted, card, onAdmin = { page = "admin" }, onSettings = { page = "settings" })
                "settings" -> SettingsScreen(dark, { dark = it }, updateMessage, { updateMessage = "Checking GitHub update…"; page = "update" }, { page = "chat" }, text, muted, card)
                "update" -> UpdateScreen(updateMessage, { page = "settings" }, text, card)
                "admin" -> AdminScreen({ page = "chat" }, text, muted, card)
            }
        }
    }
}

@Composable
private fun AuthScreen(page: String, navigate: (String) -> Unit, toggleDark: () -> Unit, dark: Boolean, text: Color, muted: Color, card: Color, onSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val title = when (page) { "login" -> "Welcome back"; "signup" -> "Create your account"; else -> "Meet your new AI" }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF29204D), if (dark) DarkBg else Color(0xFFF5F5FC))))) {
        IconButton(onClick = toggleDark, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) { Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle theme", tint = text) }
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(76.dp).background(Brush.linearGradient(listOf(Purple, Color(0xFF4BC8F5))), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(40.dp)) }
            Spacer(Modifier.height(18.dp))
            Text("HP AI ASSISTANT", color = text, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text(title, color = muted, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
            Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (page != "welcome") {
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                        Button(onClick = { if (email.contains("@") && password.length >= 4) onSuccess(email) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text(if (page == "login") "Login" else "Create account") }
                        Text("Demo only: authentication is not connected to a secure server yet.", color = muted, fontSize = 11.sp)
                    } else {
                        Text("Ask questions, brainstorm ideas, and get help with everyday tasks.", color = muted)
                        Button(onClick = { navigate("login") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Login") }
                        OutlinedButton(onClick = { navigate("signup") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Create account") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(email: String, dark: Boolean, text: Color, muted: Color, card: Color, onAdmin: () -> Unit, onSettings: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Hi! I'm HP AI Assistant. What would you like help with?") }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(Brush.linearGradient(listOf(Purple, Color(0xFF4BC8F5))), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = Color.White) }
            Column(Modifier.weight(1f).padding(start = 10.dp)) { Text("HP AI ASSISTANT", color = text, fontWeight = FontWeight.Bold); Text("Your smart companion", color = muted, fontSize = 12.sp) }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings", tint = text) }
        }
        Spacer(Modifier.height(24.dp))
        Text("What can I help with?", color = text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Signed in as $email", color = muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp, bottom = 14.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { msg -> Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(18.dp)) { Text(msg, color = text, modifier = Modifier.padding(14.dp)) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            Suggestion("✦ Explain", { prompt = "Explain a topic simply" }, card, text)
            Suggestion("✦ Ideas", { prompt = "Give me creative ideas" }, card, text)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = prompt, onValueChange = { prompt = it }, modifier = Modifier.weight(1f), placeholder = { Text("Ask anything…") }, shape = RoundedCornerShape(18.dp), maxLines = 3)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { if (prompt.isNotBlank()) { messages.add("You: $prompt"); messages.add("Demo reply: AI service is not connected yet. Connect a secure backend to get real AI answers."); prompt = "" } }, modifier = Modifier.background(Purple, CircleShape)) { Icon(Icons.Default.ArrowUpward, "Send", tint = Color.White) }
        }
        TextButton(onClick = onAdmin, modifier = Modifier.align(Alignment.End)) { Text("Admin demo", color = muted, fontSize = 11.sp) }
    }
}

@Composable
private fun Suggestion(label: String, onClick: () -> Unit, card: Color, text: Color) { Surface(onClick = onClick, color = card, shape = RoundedCornerShape(12.dp)) { Text(label, color = text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), fontSize = 12.sp) } }

@Composable
private fun SettingsScreen(dark: Boolean, setDark: (Boolean) -> Unit, updateMessage: String, checkUpdate: () -> Unit, back: () -> Unit, text: Color, muted: Color, card: Color) {
    var status by remember { mutableStateOf(updateMessage) }
    var checking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, null, tint = text) }; Text("Settings", color = text, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        Card(colors = CardDefaults.cardColors(containerColor = card)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Dark mode", color = text, fontWeight = FontWeight.SemiBold); Text("Choose your appearance", color = muted, fontSize = 12.sp) }; Switch(checked = dark, onCheckedChange = setDark) } }
        Card(colors = CardDefaults.cardColors(containerColor = card)) { Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("App updates", color = text, fontWeight = FontWeight.SemiBold)
            Text("Check GitHub for a required update", color = muted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Button(enabled = !checking, onClick = {
                checking = true
                status = "Checking GitHub…"
                scope.launch {
                    status = withContext(Dispatchers.IO) {
                        try {
                            val rawUrl = "https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPOSITORY/main/update.json"
                            val json = JSONObject(URL(rawUrl).openConnection().apply { connectTimeout = 5000; readTimeout = 5000 }.getInputStream().bufferedReader().use { it.readText() })
                            val currentVersion = 1
                            val minimum = json.optInt("minimumVersionCode", 1)
                            val latest = json.optInt("latestVersionCode", currentVersion)
                            val versionName = json.optString("latestVersionName", "new version")
                            when {
                                minimum > currentVersion -> "UPDATE REQUIRED: Install version $versionName to continue."
                                latest > currentVersion -> "Update available: version $versionName."
                                else -> "You're using the latest required version."
                            }
                        } catch (_: Exception) { "Could not check update. Edit the GitHub URL in MainActivity.kt first." }
                    }
                    checking = false
                }
            }) { Text(if (checking) "Checking…" else "Check for update") }
            if (status.isNotBlank()) Text(status, color = muted, modifier = Modifier.padding(top = 6.dp))
        } }
        Text("Version 1.0.0", color = muted)
    }
}

@Composable
private fun UpdateScreen(message: String, back: () -> Unit, text: Color, card: Color) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.SystemUpdate, null, tint = Purple, modifier = Modifier.size(64.dp))
        Text("Update status", color = text, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        Text(message.ifBlank { "Configure the GitHub update URL in MainActivity.kt to enable remote checks." }, color = text, modifier = Modifier.padding(16.dp))
        Button(onClick = back) { Text("Back to settings") }
    }
}

@Composable
private fun AdminScreen(back: () -> Unit, text: Color, muted: Color, card: Color) {
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, null, tint = text) }; Text("Admin panel (demo)", color = text, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        Card(colors = CardDefaults.cardColors(containerColor = card)) { Column(Modifier.padding(16.dp)) { Text("Dashboard", color = text, fontWeight = FontWeight.Bold); Text("This is a visual demo only. It is not protected by real admin authentication.", color = muted, modifier = Modifier.padding(top = 8.dp)) } }
        Card(colors = CardDefaults.cardColors(containerColor = card)) { Column(Modifier.padding(16.dp)) { Text("App controls", color = text, fontWeight = FontWeight.Bold); Text("• User count: demo\n• Update policy: GitHub JSON\n• Service status: backend not connected", color = muted, modifier = Modifier.padding(top = 8.dp)) } }
    }
}
