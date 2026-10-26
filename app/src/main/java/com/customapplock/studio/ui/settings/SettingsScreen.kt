package com.customapplock.studio.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import com.customapplock.studio.data.AppSettings
import com.customapplock.studio.data.SecretCodec
import com.customapplock.studio.data.SettingsStore
import com.customapplock.studio.domain.RelockMode
import com.customapplock.studio.service.GuardService
import com.customapplock.studio.ui.common.SettingItem
import com.customapplock.studio.ui.common.SettingsGroup
import com.customapplock.studio.ui.common.lockSwitchColors
import com.customapplock.studio.ui.lock.Biometrics
import com.customapplock.studio.ui.theme.Palette
import com.customapplock.studio.util.DeviceAdmin
import com.customapplock.studio.util.Permissions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore
) : ViewModel() {
    val settings: StateFlow<AppSettings> = store.state

    fun update(t: (AppSettings) -> AppSettings) = store.update(t)

    fun saveQuestion(q: String, a: String) {
        viewModelScope.launch {
            val hash = withContext(Dispatchers.Default) { SecretCodec.hash(a) }
            store.update { it.copy(securityQuestion = q.trim(), securityAnswerHash = hash) }
        }
    }
}

fun rateApp(ctx: Context) {
    val pkg = ctx.packageName
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

@Composable
fun SettingsScreen(
    onPermissions: () -> Unit,
    onMasterSetup: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val s by vm.settings.collectAsState()
    var dialog by remember { mutableStateOf<String?>(null) }
    val bioOk = remember { Biometrics.available(ctx) }
    var adminOn by remember { mutableStateOf(DeviceAdmin.isActive(ctx)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { adminOn = DeviceAdmin.isActive(ctx) }
    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.update { it.copy(intruderSelfie = true) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text("Settings", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                Text(
                    "Make Lock work exactly the way you want",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SettingsGroup("General") {
                SettingItem(
                    "App lock",
                    if (s.lockEnabled) "On. Selected apps are protected" else "Off. Nothing is locked",
                    trailing = {
                        Switch(checked = s.lockEnabled, colors = lockSwitchColors(), onCheckedChange = { on ->
                            vm.update { it.copy(lockEnabled = on) }
                            if (on) GuardService.start(ctx) else GuardService.stop(ctx)
                        })
                    }
                )
                SettingItem(
                    "Relock timing",
                    if (s.relockMode == RelockMode.GRACE) "${s.relockMode.title} · ${s.graceSeconds}s" else s.relockMode.title,
                    divider = false,
                    onClick = { dialog = "relock" }
                )
            }
            SettingsGroup("Security") {
                SettingItem(
                    "Biometric unlock",
                    if (bioOk) "Use fingerprint or face as a shortcut" else "Not available on this device",
                    trailing = {
                        Switch(
                            checked = s.biometric && bioOk, enabled = bioOk, colors = lockSwitchColors(),
                            onCheckedChange = { on -> vm.update { it.copy(biometric = on) } }
                        )
                    }
                )
                SettingItem("Change master lock", "The lock that protects this app itself", onClick = onMasterSetup)
                SettingItem(
                    "Recovery question",
                    if (s.securityQuestion.isBlank()) "Not set. Set one so you never get locked out" else s.securityQuestion,
                    onClick = { dialog = "question" }
                )
                SettingItem(
                    "Hide drawing trail",
                    "Hides the pattern line and swipe arrows so nobody can watch you unlock",
                    trailing = {
                        Switch(checked = s.stealth, colors = lockSwitchColors(), onCheckedChange = { on -> vm.update { it.copy(stealth = on) } })
                    }
                )
                SettingItem(
                    "Block screenshots",
                    "Stops screenshots and recordings of the lock screen",
                    trailing = {
                        Switch(checked = s.secureLock, colors = lockSwitchColors(), onCheckedChange = { on -> vm.update { it.copy(secureLock = on) } })
                    }
                )
                SettingItem(
                    "Uninstall protection",
                    "Nobody can uninstall Lock while this is on. Also lock the Settings app for full safety",
                    divider = false,
                    trailing = {
                        Switch(checked = adminOn, colors = lockSwitchColors(), onCheckedChange = { on ->
                            if (on) {
                                ctx.startActivity(DeviceAdmin.activateIntent(ctx))
                            } else {
                                DeviceAdmin.deactivate(ctx)
                                adminOn = false
                            }
                        })
                    }
                )
            }
            SettingsGroup("Intruder alerts") {
                SettingItem(
                    "Intruder selfie",
                    "Take a silent photo after wrong attempts",
                    trailing = {
                        Switch(checked = s.intruderSelfie, colors = lockSwitchColors(), onCheckedChange = { on ->
                            if (!on) vm.update { it.copy(intruderSelfie = false) }
                            else if (Permissions.check(ctx).camera) vm.update { it.copy(intruderSelfie = true) }
                            else camLauncher.launch(Manifest.permission.CAMERA)
                        })
                    }
                )
                SettingItem(
                    "Attempts before capture", "${s.intruderAttempts} wrong tries",
                    divider = false, onClick = { dialog = "attempts" }
                )
            }
            SettingsGroup("More") {
                SettingItem("Permissions", "Review what Lock can do", onClick = onPermissions)
                SettingItem("About Lock", "Version, architecture, credits", onClick = onAbout)
                SettingItem("Rate us", "Leave a rating on Google Play", onClick = { rateApp(ctx) })
                SettingItem("Privacy policy", null, divider = false, onClick = onPrivacy)
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    when (dialog) {
        "relock" -> RelockDialog(s.relockMode, s.graceSeconds, { dialog = null }) { m, g ->
            vm.update { it.copy(relockMode = m, graceSeconds = g) }; dialog = null
        }
        "attempts" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Attempts before capture") },
            text = {
                Column {
                    (2..5).forEach { n ->
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.update { it.copy(intruderAttempts = n) }; dialog = null }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = s.intruderAttempts == n, onClick = { vm.update { it.copy(intruderAttempts = n) }; dialog = null })
                            Text("$n wrong tries")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Close") } }
        )
        "question" -> QuestionDialog({ dialog = null }) { q, a -> vm.saveQuestion(q, a); dialog = null }
    }
}

@Composable
private fun RelockDialog(mode: RelockMode, grace: Int, onDismiss: () -> Unit, onSave: (RelockMode, Int) -> Unit) {
    var m by remember { mutableStateOf(mode) }
    var g by remember { mutableIntStateOf(grace) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Relock apps") },
        text = {
            Column {
                RelockMode.entries.forEach { opt ->
                    Row(Modifier.fillMaxWidth().clickable { m = opt }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = m == opt, onClick = { m = opt })
                        Column {
                            Text(opt.title)
                            Text(opt.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (m == RelockMode.GRACE) {
                    Text("Grace period: ${g}s", modifier = Modifier.padding(top = 8.dp))
                    Slider(value = g.toFloat(), onValueChange = { g = ((it / 5).roundToInt() * 5).coerceAtLeast(5) }, valueRange = 5f..300f)
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(m, g) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun QuestionDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var q by remember { mutableStateOf("") }
    var a by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recovery question") },
        text = {
            Column {
                OutlinedTextField(q, { q = it }, label = { Text("Question (e.g. First pet's name?)") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(a, { a = it }, label = { Text("Answer (at least 4 letters)") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(enabled = q.isNotBlank() && a.trim().length >= 4, onClick = { onSave(q, a) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
