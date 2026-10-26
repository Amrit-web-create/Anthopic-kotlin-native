package com.customapplock.studio.ui.lock

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.customapplock.studio.data.AppLockEntity
import com.customapplock.studio.domain.LockType
import com.customapplock.studio.domain.MASTER_PKG
import com.customapplock.studio.ui.common.AppIcon
import com.customapplock.studio.ui.common.BannerAd
import com.customapplock.studio.ui.common.Glyph
import com.customapplock.studio.ui.common.GlyphIcon
import com.customapplock.studio.ui.common.LocalStealth
import com.customapplock.studio.ui.common.findFragmentActivity
import com.customapplock.studio.ui.common.rememberHaptics
import com.customapplock.studio.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The shared lock screen. Used by LockActivity (with the banner ad) and as the in-app gate that
 * protects Lock itself (no ad inside the main app).
 */
@Composable
fun LockScreenHost(
    pkg: String,
    showAd: Boolean,
    onUnlocked: () -> Unit,
    vm: GateViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val activity = remember(ctx) { ctx.findFragmentActivity() }
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val settings by vm.settings.state.collectAsState()

    var entity by remember(pkg) { mutableStateOf<AppLockEntity?>(null) }
    LaunchedEffect(pkg) {
        val e = vm.entity(pkg)
        if (e == null) onUnlocked() else entity = e
    }

    val label = remember(pkg) { vm.label(pkg) }
    var resetKey by remember { mutableIntStateOf(0) }
    var failed by remember(pkg) { mutableIntStateOf(vm.failuresOf(pkg)) }
    var remaining by remember(pkg) { mutableStateOf(vm.remainingMs(pkg)) }
    var message by remember(pkg) { mutableStateOf<String?>(null) }
    var showForgot by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }
    val frozen = remaining > 0L

    // countdown while the lock is frozen after too many wrong tries
    LaunchedEffect(pkg, frozen) {
        if (frozen) {
            while (vm.remainingMs(pkg) > 0L) {
                remaining = vm.remainingMs(pkg)
                delay(250)
            }
            remaining = 0L
            message = null
            resetKey++
        }
    }

    val bioAvailable = remember { Biometrics.available(ctx) }
    val canBio = settings.biometric && bioAvailable && activity != null
    LaunchedEffect(entity, canBio) {
        if (entity != null && canBio) Biometrics.prompt(activity!!, "Unlock $label") { vm.onSuccess(pkg); onUnlocked() }
    }

    val handle: (String) -> Unit = { secret ->
        val e = entity
        if (e != null && !frozen) scope.launch {
            if (vm.verify(e, secret)) {
                haptics.success()
                vm.onSuccess(pkg)
                onUnlocked()
            } else {
                haptics.error()
                val n = vm.onFailure(pkg)
                failed = n
                val left = 5 - (n % 5)
                if (n % 5 == 0) {
                    remaining = vm.remainingMs(pkg)
                    message = "Too many wrong attempts"
                } else {
                    message = "Wrong passcode. $left ${if (left == 1) "try" else "tries"} left before lockout"
                }
                vm.onWrong(pkg, n, activity)
                repeat(3) {
                    shake.animateTo(if (it % 2 == 0) 16f else -16f, tween(50))
                }
                shake.animateTo(0f, tween(50))
                delay(250)
                resetKey++
            }
        }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A1530), Palette.Bg, Color(0xFF060B18)))
        )
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(Palette.Blue.copy(alpha = .16f)))
                AppIcon(pkg, 56.dp, vm::icon)
            }
            Spacer(Modifier.height(10.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            val type = entity?.let { LockType.fromId(it.lockTypeId) }
            Text(
                message ?: type?.hint.orEmpty(),
                color = if (message != null) Palette.Rose else Color(0xFF8EA0BE),
                fontSize = 14.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                val used = failed % 5
                for (i in 0 until 5) {
                    Box(
                        Modifier.size(7.dp).clip(CircleShape)
                            .background(if (i < used) Palette.Rose else Color.White.copy(alpha = .14f))
                    )
                }
            }
            Box(
                Modifier.weight(1f).fillMaxWidth().offset { IntOffset(shake.value.roundToInt(), 0) },
                contentAlignment = Alignment.Center
            ) {
                if (frozen) {
                    val secs = (remaining / 1000L).toInt() + 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GlyphIcon(Glyph.LOCK, iconSize = 54.dp, tint = Palette.Rose)
                        Spacer(Modifier.height(14.dp))
                        Text("Locked for security", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Try again in %d:%02d".format(secs / 60, secs % 60),
                            color = Color(0xFF8EA0BE), fontSize = 16.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                } else if (type != null) {
                    CompositionLocalProvider(LocalStealth provides settings.stealth) {
                        LockInput(type, resetKey, handle)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showForgot = true }, enabled = !frozen) {
                    Text("Forgot passcode?", color = Palette.BlueLight)
                }
                if (canBio) {
                    TextButton(onClick = { Biometrics.prompt(activity!!, "Unlock $label") { vm.onSuccess(pkg); onUnlocked() } }) {
                        Text("Use biometrics", color = Palette.BlueLight)
                    }
                }
            }
            if (showAd && pkg != MASTER_PKG) BannerAd()
        }
    }

    if (showForgot) {
        var answer by remember { mutableStateOf("") }
        var wrong by remember { mutableStateOf(false) }
        val hasQuestion = settings.securityQuestion.isNotBlank()
        AlertDialog(
            onDismissRequest = { showForgot = false },
            title = { Text("Recover access") },
            text = {
                Column {
                    if (!hasQuestion) {
                        Text("No recovery question has been set. You can set one in Settings > Security.")
                    } else {
                        Text(settings.securityQuestion, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = answer, onValueChange = { answer = it; wrong = false },
                            singleLine = true, isError = wrong, label = { Text("Your answer") }
                        )
                        if (wrong) Text("That answer doesn't match.", color = Palette.Rose, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                if (hasQuestion) {
                    TextButton(onClick = {
                        scope.launch {
                            if (vm.checkAnswer(answer)) {
                                showForgot = false
                                vm.onSuccess(pkg)
                                onUnlocked()
                            } else {
                                wrong = true
                                val n = vm.onFailure(pkg)
                                failed = n
                                if (n % 5 == 0) { remaining = vm.remainingMs(pkg); showForgot = false }
                            }
                        }
                    }) { Text("Verify") }
                }
            },
            dismissButton = { TextButton(onClick = { showForgot = false }) { Text("Close") } }
        )
    }
}
