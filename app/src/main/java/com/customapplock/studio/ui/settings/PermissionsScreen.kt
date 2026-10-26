package com.customapplock.studio.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.customapplock.studio.ui.common.Glyph
import com.customapplock.studio.ui.common.GlyphIcon
import com.customapplock.studio.ui.theme.Palette
import com.customapplock.studio.util.Permissions

private enum class PStep(
    val glyph: Glyph,
    val title: String,
    val body: String,
    val actionLabel: String,
    val required: Boolean,
    val showTip: Boolean
) {
    ACCESS(
        Glyph.ACCESS, "Turn on Accessibility",
        "Lock uses it only to notice which app just opened, so your lock screen can appear. It never reads your screen, messages or passwords.",
        "Open Accessibility settings", true, true
    ),
    OVERLAY(
        Glyph.OVERLAY, "Allow display over other apps",
        "This lets your lock screen appear instantly on top of a protected app, before anyone can see what is inside.",
        "Open display settings", true, true
    ),
    NOTIF(
        Glyph.BELL, "Allow notifications",
        "A quiet notification keeps protection running reliably in the background. Nothing else is ever sent.",
        "Allow notifications", false, false
    ),
    CAMERA(
        Glyph.CAMERA, "Allow camera",
        "Optional. Used only to take a silent photo of anyone who enters the wrong passcode. Photos never leave your phone.",
        "Allow camera", false, false
    )
}

/** One permission per full screen: grant it, then press Confirm to move on. */
@Composable
fun PermissionsScreen(onDone: () -> Unit, onBack: (() -> Unit)?) {
    val ctx = LocalContext.current
    var st by remember { mutableStateOf(Permissions.check(ctx)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { st = Permissions.check(ctx) }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { st = Permissions.check(ctx) }
    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { st = Permissions.check(ctx) }

    val steps = PStep.entries
    fun granted(s: PStep): Boolean = when (s) {
        PStep.ACCESS -> st.accessibility
        PStep.OVERLAY -> st.overlay
        PStep.NOTIF -> st.notifications
        PStep.CAMERA -> st.camera
    }
    var index by rememberSaveable {
        mutableStateOf(steps.indexOfFirst { !granted(it) }.let { if (it < 0) 0 else it })
    }
    val step = steps[index]
    val ok = granted(step)

    val fade = remember { Animatable(0f) }
    LaunchedEffect(index) {
        fade.snapTo(0f)
        fade.animateTo(1f, tween(380))
    }
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        0.88f, 1.1f, infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "g"
    )

    val openSettings: () -> Unit = {
        when (step) {
            PStep.ACCESS -> ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            PStep.OVERLAY -> ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            PStep.NOTIF -> { if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            PStep.CAMERA -> camLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    val openAppInfo: () -> Unit = {
        ctx.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
    val next: () -> Unit = { if (index < steps.lastIndex) index++ else onDone() }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A1530), Palette.Bg))
        )
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // top bar: back + progress
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                } else {
                    Spacer(Modifier.width(16.dp))
                }
                Text(
                    "Step ${index + 1} of ${steps.size}",
                    color = Color(0xFF8EA0BE), style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(end = 16.dp)) {
                    for (i in steps.indices) {
                        Box(
                            Modifier.size(width = if (i == index) 26.dp else 10.dp, height = 8.dp).clip(CircleShape)
                                .background(if (i <= index) Palette.Blue else Palette.Border)
                        )
                    }
                }
            }

            // content
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .graphicsLayer { alpha = fade.value }
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f
                        drawCircle(Palette.Blue.copy(alpha = .10f), r * glow)
                        drawCircle(Palette.Blue.copy(alpha = .16f), r * 0.74f * glow)
                    }
                    Box(
                        Modifier.size(104.dp).clip(RoundedCornerShape(34.dp))
                            .background(Brush.linearGradient(listOf(Palette.BlueLight, Palette.BlueDeep))),
                        contentAlignment = Alignment.Center
                    ) { GlyphIcon(step.glyph, iconSize = 54.dp, tint = Color.White) }
                }
                Spacer(Modifier.height(22.dp))
                Text(step.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    step.body, style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF9FB0CC), textAlign = TextAlign.Center
                )
                if (ok) {
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlyphIcon(Glyph.CHECK, iconSize = 22.dp, tint = Palette.Ok)
                        Spacer(Modifier.width(8.dp))
                        Text("Permission granted", color = Palette.Ok, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // bottom sheet
            Surface(
                color = Palette.Surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                border = BorderStroke(1.dp, Palette.Border)
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
                    if (step.showTip && !ok) {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(Palette.SurfaceHigh)
                                .border(1.dp, Palette.Border, RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Text("If it does not work", color = Palette.BlueLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Open App info, tap the 3-dot menu (⋮) at the top-right and turn on “Allow restricted settings”. Then come back here and try again.",
                                color = Color(0xFF9FB0CC), fontSize = 14.sp, lineHeight = 20.sp
                            )
                            TextButton(onClick = openAppInfo, contentPadding = PaddingValues(0.dp)) {
                                Text("Open App info", color = Palette.BlueLight, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                    if (!ok) {
                        Button(
                            onClick = openSettings,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.Blue, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        ) { Text(step.actionLabel, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(10.dp))
                    }
                    Button(
                        onClick = next,
                        enabled = ok,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ok) Palette.Blue else Palette.SurfaceHigh,
                            contentColor = Color.White,
                            disabledContainerColor = Palette.SurfaceHigh,
                            disabledContentColor = Color(0xFF5E6E8C)
                        ),
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        Text(
                            if (index == steps.lastIndex) "Confirm and finish" else "Confirm",
                            fontSize = 17.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    if (!step.required && !ok) {
                        TextButton(onClick = next, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Skip for now", color = Color(0xFF8EA0BE), fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
