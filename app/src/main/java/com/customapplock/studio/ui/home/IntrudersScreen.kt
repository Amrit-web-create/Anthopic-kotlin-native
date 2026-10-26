package com.customapplock.studio.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.exifinterface.media.ExifInterface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.customapplock.studio.data.AppsRepository
import com.customapplock.studio.data.IntruderEntity
import com.customapplock.studio.data.LockRepository
import com.customapplock.studio.ui.common.EmptyState
import com.customapplock.studio.ui.common.Glyph
import com.customapplock.studio.ui.common.GlyphIcon
import com.customapplock.studio.ui.theme.Palette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class IntrudersViewModel @Inject constructor(
    private val repo: LockRepository,
    private val apps: AppsRepository
) : ViewModel() {
    val logs: StateFlow<List<IntruderEntity>> =
        repo.intruders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun label(pkg: String) = apps.label(pkg)
    fun delete(e: IntruderEntity) { viewModelScope.launch { repo.deleteIntruder(e) } }
    fun clear() { viewModelScope.launch { repo.clearIntruders() } }
}

private fun loadThumb(path: String): ImageBitmap? {
    val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 }) ?: return null
    val deg = when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (deg == 0f) return bmp.asImageBitmap()
    val m = Matrix().apply { postRotate(deg) }
    return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true).asImageBitmap()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntrudersScreen(vm: IntrudersViewModel = hiltViewModel()) {
    val logs by vm.logs.collectAsState()
    val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Intruders", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text(
                        "Photos and times of wrong attempts",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (logs.isNotEmpty()) TextButton(onClick = { vm.clear() }) { Text("Clear all") }
            }
            if (logs.isEmpty()) {
                EmptyState(Glyph.EYE, "No break-in attempts", "Turn on intruder selfie in Settings. Wrong attempts will be logged here.")
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logs, key = { it.id }) { e ->
                        val thumb by produceState<ImageBitmap?>(null, e.photoPath) {
                            value = e.photoPath?.let { p -> withContext(Dispatchers.IO) { runCatching { loadThumb(p) }.getOrNull() } }
                        }
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Palette.Surface)
                                .border(1.dp, Palette.Border, RoundedCornerShape(20.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).background(Palette.SurfaceHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                val t = thumb
                                if (t != null) Image(t, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                else GlyphIcon(Glyph.CAMERA, iconSize = 30.dp, tint = Color(0xFF8EA0BE))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(vm.label(e.packageName), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    fmt.format(Date(e.timestamp)), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (e.photoPath == null) {
                                    Text("No photo (camera permission off)", style = MaterialTheme.typography.bodySmall, color = Palette.Rose)
                                }
                            }
                            IconButton(onClick = { vm.delete(e) }) { Icon(Icons.Default.Delete, "Delete") }
                        }
                    }
                }
            }
        }
    }
}
