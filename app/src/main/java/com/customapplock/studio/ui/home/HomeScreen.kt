package com.customapplock.studio.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.customapplock.studio.ui.common.AppIcon
import com.customapplock.studio.ui.common.LockTypeIcon
import com.customapplock.studio.ui.common.ShieldBadge
import com.customapplock.studio.ui.common.lockSwitchColors
import com.customapplock.studio.ui.theme.Palette
import com.customapplock.studio.util.Permissions

private val SUGGESTED = setOf(
    "com.whatsapp", "com.whatsapp.w4b", "com.instagram.android", "com.facebook.katana", "com.facebook.orca",
    "org.telegram.messenger", "com.snapchat.android", "com.google.android.apps.photos", "com.google.android.gm",
    "com.android.settings", "com.android.vending", "com.google.android.apps.messaging", "com.android.chrome",
    "com.google.android.youtube", "com.phonepe.app", "com.google.android.apps.nbu.paisa.user", "net.one97.paytm"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onSetup: (List<String>) -> Unit,
    onPermissions: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val rows by vm.rows.collectAsState()
    val loading by vm.loading.collectAsState()
    val query by vm.query.collectAsState()
    val filter by vm.filter.collectAsState()
    val lockedCount by vm.lockedCount.collectAsState()
    val settings by vm.settings.state.collectAsState()
    var perms by remember { mutableStateOf(Permissions.check(ctx)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { perms = Permissions.check(ctx) }

    val selected = remember { mutableStateListOf<String>() }
    val selecting = selected.isNotEmpty()
    BackHandler(selecting) { selected.clear() }
    val suggested = remember(rows) { rows.filter { it.pkg in SUGGESTED && !it.locked }.take(12) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (selecting) {
                TopAppBar(
                    title = { Text("${selected.size} selected") },
                    navigationIcon = { IconButton(onClick = { selected.clear() }) { Icon(Icons.Default.Close, "Cancel") } },
                    actions = {
                        TextButton(onClick = { selected.clear(); selected.addAll(rows.map { it.pkg }) }) { Text("All") }
                        TextButton(onClick = { onSetup(selected.toList()); selected.clear() }) { Text("Lock") }
                    }
                )
            }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("Lock", style = MaterialTheme.typography.displaySmall, color = Color.White)
                    Text(
                        "Only you can open what matters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF0B1B45), Palette.Bg)))
                        .border(1.dp, Palette.Border, RoundedCornerShape(28.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                when {
                                    !perms.required -> "Setup needed"
                                    !settings.lockEnabled -> "Protection paused"
                                    else -> "Protection active"
                                },
                                color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                if (lockedCount == 1) "1 app locked" else "$lockedCount apps locked",
                                color = Color.White.copy(alpha = .75f), fontSize = 14.sp
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        vm.lockNow()
                                        android.widget.Toast.makeText(ctx, "All apps are locked again", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Palette.BlueDeep)
                                ) { Text("Lock all now", fontWeight = FontWeight.Bold) }
                                if (!perms.required) {
                                    Button(
                                        onClick = onPermissions,
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Palette.Rose, contentColor = Color.White)
                                    ) { Text("Fix", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                        ShieldBadge(active = perms.required && settings.lockEnabled)
                    }
                }
            }
            if (suggested.isNotEmpty() && query.isBlank() && !selecting) {
                item {
                    Text(
                        "SUGGESTED", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                        color = Palette.BlueLight, modifier = Modifier.padding(start = 24.dp, top = 14.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(suggested, key = { it.pkg }) { row ->
                            Column(
                                Modifier.width(78.dp).clip(RoundedCornerShape(18.dp))
                                    .background(Palette.Surface)
                                    .border(1.dp, Palette.Border, RoundedCornerShape(18.dp))
                                    .clickable { if (row.hasConfig) vm.toggle(row.pkg, true) else onSetup(listOf(row.pkg)) }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AppIcon(row.pkg, 46.dp, vm::icon)
                                Text(
                                    row.label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query, onValueChange = { vm.query.value = it },
                    placeholder = { Text("Search apps") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            item {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppFilter.entries.forEach {
                        FilterChip(selected = filter == it, onClick = { vm.filter.value = it }, label = { Text(it.title) })
                    }
                }
            }
            if (loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
            }
            items(rows, key = { it.pkg }) { row ->
                val isSel = row.pkg in selected
                val toggle: (Boolean) -> Unit = { on ->
                    if (on && !row.hasConfig) onSetup(listOf(row.pkg)) else vm.toggle(row.pkg, on)
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) Color(0xFF12306B) else Palette.Surface)
                        .border(1.dp, if (isSel) Palette.BlueLight else Palette.Border, RoundedCornerShape(20.dp))
                        .combinedClickable(
                            onClick = {
                                if (selecting) { if (isSel) selected.remove(row.pkg) else selected.add(row.pkg) }
                                else toggle(!row.locked)
                            },
                            onLongClick = { if (!isSel) selected.add(row.pkg) }
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(row.pkg, 46.dp, vm::icon)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(row.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            row.lockType?.let {
                                LockTypeIcon(it, iconSize = 14.dp, tint = if (row.locked) Palette.BlueLight else Color(0xFF8EA0BE))
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                row.lockType?.title ?: "Not protected",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (row.locked) Palette.BlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(checked = row.locked, onCheckedChange = toggle, colors = lockSwitchColors())
                }
            }
        }
    }
}
