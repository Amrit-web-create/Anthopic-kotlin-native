package com.customapplock.studio.ui.setup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.customapplock.studio.domain.HOME_PKG
import com.customapplock.studio.domain.LockType
import com.customapplock.studio.domain.MASTER_PKG
import com.customapplock.studio.ui.common.Glyph
import com.customapplock.studio.ui.common.GlyphIcon
import com.customapplock.studio.ui.common.LockTypeIcon
import com.customapplock.studio.ui.common.rememberHaptics
import com.customapplock.studio.ui.lock.LockInput
import com.customapplock.studio.ui.theme.AppLockTheme
import com.customapplock.studio.ui.theme.Palette
import kotlinx.coroutines.delay

private enum class Step { CHOOSE, INPUT, CONFIRM, PRACTICE, DONE }

/** Input -> Confirm -> 3x mandatory practice -> Activation. */
@Composable
fun SetupScreen(
    pkgs: List<String>,
    presetType: String,
    onClose: () -> Unit,
    forced: Boolean = false,
    vm: SetupViewModel = hiltViewModel()
) {
    AppLockTheme {
        val haptics = rememberHaptics()
        var typeId by rememberSaveable { mutableStateOf(presetType) }
        var step by rememberSaveable { mutableStateOf(if (presetType.isEmpty()) Step.CHOOSE else Step.INPUT) }
        var first by rememberSaveable { mutableStateOf("") }
        var practice by rememberSaveable { mutableStateOf(0) }
        var resetKey by remember { mutableIntStateOf(0) }
        var error by remember { mutableStateOf<String?>(null) }
        val type = LockType.fromId(typeId)
        val isMaster = pkgs.singleOrNull() == MASTER_PKG
        val isHome = pkgs.singleOrNull() == HOME_PKG
        BackHandler(enabled = forced) { }

        val onSecret: (String) -> Unit = { s ->
            when (step) {
                Step.INPUT -> { first = s; error = null; step = Step.CONFIRM }
                Step.CONFIRM ->
                    if (s == first) { error = null; practice = 0; step = Step.PRACTICE }
                    else { error = "Those didn't match. Start again."; first = ""; resetKey++; step = Step.INPUT }
                Step.PRACTICE ->
                    if (s == first) {
                        practice++
                        haptics.success()
                        error = null
                        if (practice >= 3) step = Step.DONE else resetKey++
                    } else {
                        practice = 0; error = "Wrong. Your streak was reset."; haptics.error(); resetKey++
                    }
                else -> {}
            }
        }

        Surface(color = Palette.Bg, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!forced) {
                        IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                    } else {
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        when {
                            forced -> "Create your master lock"
                            isMaster -> "Change master lock"
                            isHome -> "Home screen lock"
                            else -> "Set up lock"
                        },
                        color = Color.White, style = MaterialTheme.typography.titleLarge
                    )
                }
                when (step) {
                    Step.CHOOSE -> ChooseType(
                        header = if (forced) "This lock protects the Lock app itself, so nobody else can change your settings."
                        else if (isMaster) "Choose a design to protect the Lock app itself"
                        else if (isHome) "Choose a design for your home screen"
                        else "Choose a lock design for ${if (pkgs.size == 1) "this app" else "${pkgs.size} apps"}"
                    ) { typeId = it.id; step = Step.INPUT }
                    Step.DONE -> DoneView(isMaster, isHome, pkgs.size) {
                        LaunchedEffect(Unit) {
                            haptics.success()
                            if (forced) {
                                delay(1300)
                                vm.save(pkgs, type, first)
                            } else {
                                vm.save(pkgs, type, first)
                                delay(1600)
                                onClose()
                            }
                        }
                    }
                    else -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        val stepNo = when (step) { Step.INPUT -> 1; Step.CONFIRM -> 2; else -> 3 }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            for (i in 1..3) {
                                Box(
                                    Modifier.size(width = 40.dp, height = 6.dp).clip(CircleShape)
                                        .background(if (i <= stepNo) Palette.Blue else Palette.Border)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            when (step) {
                                Step.INPUT -> "Create your ${type.title} passcode"
                                Step.CONFIRM -> "Repeat it to confirm"
                                else -> "Practice unlock ${practice + 1} of 3"
                            },
                            color = Color.White, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center
                        )
                        Text(
                            error ?: if (step == Step.PRACTICE) "Unlock 3 times in a row to activate" else type.hint,
                            color = if (error != null) Palette.Rose else Color(0xFF8EA0BE),
                            fontSize = 14.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            LockInput(type, step.ordinal * 1000 + resetKey, onSecret)
                        }
                        if (step == Step.INPUT && presetType.isEmpty()) {
                            TextButton(onClick = { step = Step.CHOOSE; resetKey++ }) {
                                Text("Change lock design", color = Palette.BlueLight)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChooseType(header: String, onPick: (LockType) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text(
            header, color = Color(0xFF8EA0BE), fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(LockType.entries.chunked(2)) { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { t ->
                        Column(
                            Modifier.weight(1f).height(196.dp).clip(RoundedCornerShape(24.dp))
                                .background(Brush.verticalGradient(listOf(Palette.SurfaceHigh, Palette.Surface)))
                                .border(1.dp, Palette.Border, RoundedCornerShape(24.dp))
                                .clickable { onPick(t) }
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(58.dp).clip(RoundedCornerShape(18.dp))
                                    .background(Brush.linearGradient(listOf(Palette.Blue, Palette.BlueDeep))),
                                contentAlignment = Alignment.Center
                            ) { LockTypeIcon(t, iconSize = 32.dp) }
                            Spacer(Modifier.height(10.dp))
                            Text(t.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
                            Text(t.level, color = Palette.BlueLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(t.hint, color = Color(0xFF8EA0BE), fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 3)
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DoneView(isMaster: Boolean, isHome: Boolean, count: Int, effect: @Composable () -> Unit) {
    effect()
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(
            Modifier.size(120.dp).scale(scale.value).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Palette.BlueLight, Palette.BlueDeep))),
            contentAlignment = Alignment.Center
        ) { GlyphIcon(Glyph.CHECK, iconSize = 64.dp) }
        Spacer(Modifier.height(20.dp))
        Text("Locked and loaded!", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text(
            if (isMaster) "The Lock app is now protected"
            else if (isHome) "Your home screen is now protected"
            else if (count == 1) "1 app is now protected" else "$count apps are now protected",
            color = Color(0xFF8EA0BE), fontSize = 15.sp
        )
    }
}
