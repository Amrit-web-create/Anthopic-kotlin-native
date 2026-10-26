package com.customapplock.studio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.customapplock.studio.BuildConfig
import com.customapplock.studio.R
import com.customapplock.studio.ui.common.Glyph
import com.customapplock.studio.ui.common.GlyphIcon
import com.customapplock.studio.ui.theme.Palette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("About Lock", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(96.dp).clip(RoundedCornerShape(32.dp))
                    .background(Brush.linearGradient(listOf(Palette.BlueLight, Palette.BlueDeep))),
                contentAlignment = Alignment.Center
            ) { GlyphIcon(Glyph.LOCK, iconSize = 52.dp) }
            Spacer(Modifier.height(18.dp))
            Text("Lock", style = MaterialTheme.typography.displaySmall, color = Color.White)
            Text("Version ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(26.dp))
            Block("Architecture", "100% Kotlin. Jetpack Compose and Material 3, MVVM with clean layers, Hilt dependency injection, Room database, StateFlow and Coroutines. Passcodes are salted PBKDF2 hashes and settings live in encrypted preferences. Works fully offline.")
            Block("How it works", "A lightweight accessibility service watches only for app-switch events and instantly opens your lock design on top of the protected app. No polling, no wake locks, nothing runs while the screen is off.")
            Block("Built-in protection", "Every 5 wrong tries freeze the lock, and the freeze doubles each time. Break-in photos, hidden drawing trail, screenshot blocking and uninstall protection keep strangers out.")
            Block("Developer", "Designed and built by Amrit Bharadwaj.")
            Spacer(Modifier.height(18.dp))
            Text(
                "Copyright © Amrit Bharadwaj All Rights Reserved",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Block(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val md = remember { ctx.resources.openRawResource(R.raw.privacy_policy).bufferedReader().use { it.readText() } }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Privacy policy", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp)) {
            md.lines().forEach { line ->
                when {
                    line.startsWith("# ") -> Text(line.drop(2), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    line.startsWith("## ") -> Text(line.drop(3), style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.padding(top = 18.dp, bottom = 4.dp))
                    line.startsWith("- ") -> Text("•  " + line.drop(2).replace("**", ""), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
                    line.isBlank() -> Spacer(Modifier.height(6.dp))
                    else -> Text(line.replace("**", ""), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
