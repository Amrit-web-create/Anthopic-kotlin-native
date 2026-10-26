package com.customapplock.studio.ui

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.customapplock.studio.data.AppLockEntity
import com.customapplock.studio.data.LockRepository
import com.customapplock.studio.data.SettingsStore
import com.customapplock.studio.domain.MASTER_PKG
import com.customapplock.studio.service.GuardService
import com.customapplock.studio.service.SessionManager
import com.customapplock.studio.ui.lock.LockScreenHost
import com.customapplock.studio.ui.setup.SetupScreen
import com.customapplock.studio.ui.theme.AppLockTheme
import com.customapplock.studio.util.Permissions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repo: LockRepository,
    val session: SessionManager,
    val settings: SettingsStore
) : ViewModel() {
    /** null while loading, then whether the Lock app itself is protected by a master lock. */
    val hasMaster: StateFlow<Boolean?> = repo.all.map<List<AppLockEntity>, Boolean?> { l ->
        l.any { it.packageName == MASTER_PKG }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun markOnboarded() { settings.update { it.copy(onboarded = true) } }
}

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { AppLockTheme { Root() } }
    }

    override fun onStart() {
        super.onStart()
        session.checkMasterTimeout(30_000)
        if (Permissions.isAccessibilityOn(this)) GuardService.start(this)
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) session.markMasterLeft()
    }
}

/**
 * Entry flow: permissions wizard -> mandatory master lock -> app.
 * The master lock cannot be skipped, so nobody else can open Lock and switch protection off.
 */
@Composable
private fun Root(vm: MainViewModel = hiltViewModel()) {
    val hasMaster by vm.hasMaster.collectAsState()
    val ready by vm.settings.ready.collectAsState()
    val settings by vm.settings.state.collectAsState()
    val unlocked by vm.session.masterUnlocked.collectAsState()
    when {
        hasMaster == null || !ready -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        hasMaster == false && settings.onboarded -> SetupScreen(
            pkgs = listOf(MASTER_PKG), presetType = "", onClose = {}, forced = true
        )
        hasMaster == true && !unlocked -> LockScreenHost(
            pkg = MASTER_PKG, showAd = false, onUnlocked = { vm.session.setMasterUnlocked() }
        )
        else -> AppNav(showPermissionsFirst = !settings.onboarded, onOnboarded = { vm.markOnboarded() })
    }
}
