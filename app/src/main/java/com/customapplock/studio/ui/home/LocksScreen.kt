package com.customapplock.studio.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.customapplock.studio.data.AppLockEntity
import com.customapplock.studio.data.AppsRepository
import com.customapplock.studio.data.LockRepository
import com.customapplock.studio.domain.HOME_PKG
import com.customapplock.studio.domain.LockType
import com.customapplock.studio.domain.MASTER_PKG
import com.customapplock.studio.ui.common.AppIcon
import com.customapplock.studio.ui.common.EmptyState
import com.customapplock.studio.ui.common.Glyph
import com.customapplock.studio.ui.common.GlyphIcon
import com.customapplock.studio.ui.common.LockTypeIcon
import com.customapplock.studio.ui.common.lockSwitchColors
import com.customapplock.studio.ui.theme.Palette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignRow(val pkg: String, val label: String, val type: LockType, val locked: Boolean)

@HiltViewModel
class LocksViewModel @Inject constructor(
    private val repo: LockRepository,
    private val apps: AppsRepository
) : ViewModel() {
    val rows: StateFlow<List<AssignRow>> = repo.all.map { list ->
        list.filter { it.packageName != MASTER_PKG && it.packageName != HOME_PKG }
            .map { AssignRow(it.packageName, apps.label(it.packageName), LockType.fromId(it.lockTypeId), it.isLocked) }
            .sortedBy { it.label.lowercase() }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val home: StateFlow<AppLockEntity?> = repo.all.map { l -> l.firstOrNull { it.packageName == HOME_PKG } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setLocked(pkg: String, on: Boolean) { viewModelScope.launch { repo.setLocked(pkg, on) } }
    fun remove(pkg: String) { viewModelScope.launch { repo.remove(pkg) } }
    suspend fun icon(pkg: String) = apps.icon(pkg)
}

/** Lock assignment matrix + the home screen lock. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocksScreen(
    onChange: (pkg: String, typeId: String) -> Unit,
    onHomeSetup: () -> Unit,
    vm: LocksViewModel = hiltViewModel()
) {
    val rows by vm.rows.collectAsState()
    val home by vm.home.collectAsState()
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 4.dp, vertical = 14.dp)) {
                    Text("Locks", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text(
                        "Choose a different design for every app",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                val homeLocked = home?.isLocked == true
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF12306B), Palette.Surface)))
                        .border(1.dp, if (homeLocked) Palette.BlueLight else Palette.Border, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Palette.Blue),
                            contentAlignment = Alignment.Center
                        ) { GlyphIcon(Glyph.HOME, iconSize = 26.dp) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Home screen lock", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Ask for your lock whenever the home screen opens",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = homeLocked,
                            onCheckedChange = { on ->
                                if (on && home == null) onHomeSetup() else vm.setLocked(HOME_PKG, on)
                            },
                            colors = lockSwitchColors()
                        )
                    }
                    if (home != null) {
                        val t = LockType.fromId(home!!.lockTypeId)
                        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            LockTypeIcon(t, iconSize = 16.dp, tint = Palette.BlueLight)
                            Spacer(Modifier.width(8.dp))
                            Text(t.title, color = Palette.BlueLight, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            TextButton(onClick = onHomeSetup) { Text("Change design") }
                        }
                    }
                }
            }
            item {
                Text(
                    "PROTECTED APPS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                    color = Palette.BlueLight, modifier = Modifier.padding(start = 8.dp, top = 10.dp)
                )
            }
            if (rows.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        EmptyState(Glyph.LOCK, "No locked apps yet", "Open the Apps tab and flip a switch to protect an app.")
                    }
                }
            }
            items(rows, key = { it.pkg }) { r ->
                var open by remember { mutableStateOf(false) }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Palette.Surface)
                        .border(1.dp, Palette.Border, RoundedCornerShape(20.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(r.pkg, 44.dp, vm::icon)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Box {
                            TextButton(onClick = { open = true }, contentPadding = PaddingValues(0.dp)) {
                                LockTypeIcon(r.type, iconSize = 15.dp, tint = Palette.BlueLight)
                                Spacer(Modifier.width(6.dp))
                                Text("${r.type.title}  ▾", color = Palette.BlueLight)
                            }
                            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                LockType.entries.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.title) },
                                        leadingIcon = { LockTypeIcon(t, iconSize = 20.dp, tint = Palette.BlueLight) },
                                        onClick = { open = false; onChange(r.pkg, t.id) }
                                    )
                                }
                            }
                        }
                    }
                    Switch(checked = r.locked, onCheckedChange = { vm.setLocked(r.pkg, it) }, colors = lockSwitchColors())
                    IconButton(onClick = { vm.remove(r.pkg) }) { Icon(Icons.Default.Delete, "Remove lock") }
                }
            }
        }
    }
}
