package com.customapplock.studio.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.customapplock.studio.domain.HOME_PKG
import com.customapplock.studio.domain.MASTER_PKG
import com.customapplock.studio.ui.home.HomeScreen
import com.customapplock.studio.ui.home.IntrudersScreen
import com.customapplock.studio.ui.home.LocksScreen
import com.customapplock.studio.ui.settings.AboutScreen
import com.customapplock.studio.ui.settings.PermissionsScreen
import com.customapplock.studio.ui.settings.PrivacyScreen
import com.customapplock.studio.ui.settings.SettingsScreen
import com.customapplock.studio.ui.setup.SetupScreen
import com.customapplock.studio.ui.theme.Palette

object Routes {
    const val HOME = "home"
    const val LOCKS = "locks"
    const val INTRUDERS = "intruders"
    const val SETTINGS = "settings"
    const val PERMS = "permissions"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val SETUP = "setup/{pkgs}?type={type}"
    fun setup(pkgs: List<String>, type: String = "") = "setup/${Uri.encode(pkgs.joinToString(","))}?type=$type"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Apps", Icons.Default.Home),
    Tab(Routes.LOCKS, "Locks", Icons.Default.Lock),
    Tab(Routes.INTRUDERS, "Intruders", Icons.Default.Warning),
    Tab(Routes.SETTINGS, "Settings", Icons.Default.Settings)
)

@Composable
fun AppNav(showPermissionsFirst: Boolean, onOnboarded: () -> Unit) {
    val nav = rememberNavController()
    val start = remember { if (showPermissionsFirst) Routes.PERMS else Routes.HOME }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.route == route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = Palette.Surface, tonalElevation = 0.dp) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = {
                                nav.navigate(t.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(t.icon, t.label) },
                            label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Palette.Blue,
                                selectedIconColor = Color.White,
                                selectedTextColor = Palette.BlueLight,
                                unselectedIconColor = Color(0xFF8EA0BE),
                                unselectedTextColor = Color(0xFF8EA0BE)
                            )
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(nav, startDestination = start, modifier = Modifier.padding(inner).consumeWindowInsets(inner)) {
            composable(Routes.HOME) {
                HomeScreen(
                    onSetup = { nav.navigate(Routes.setup(it)) },
                    onPermissions = { nav.navigate(Routes.PERMS) }
                )
            }
            composable(Routes.LOCKS) {
                LocksScreen(
                    onChange = { pkg, type -> nav.navigate(Routes.setup(listOf(pkg), type)) },
                    onHomeSetup = { nav.navigate(Routes.setup(listOf(HOME_PKG))) }
                )
            }
            composable(Routes.INTRUDERS) { IntrudersScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onPermissions = { nav.navigate(Routes.PERMS) },
                    onMasterSetup = { nav.navigate(Routes.setup(listOf(MASTER_PKG))) },
                    onAbout = { nav.navigate(Routes.ABOUT) },
                    onPrivacy = { nav.navigate(Routes.PRIVACY) }
                )
            }
            composable(Routes.PERMS) {
                val canBack = nav.previousBackStackEntry != null
                PermissionsScreen(
                    onDone = {
                        onOnboarded()
                        if (canBack) nav.popBackStack()
                        else nav.navigate(Routes.HOME) { popUpTo(Routes.PERMS) { inclusive = true } }
                    },
                    onBack = if (canBack) ({ nav.popBackStack() }) else null
                )
            }
            composable(Routes.ABOUT) { AboutScreen { nav.popBackStack() } }
            composable(Routes.PRIVACY) { PrivacyScreen { nav.popBackStack() } }
            composable(
                Routes.SETUP,
                arguments = listOf(
                    navArgument("pkgs") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType; defaultValue = "" }
                )
            ) { e ->
                val pkgs = e.arguments?.getString("pkgs").orEmpty().split(",").filter { it.isNotBlank() }
                val type = e.arguments?.getString("type").orEmpty()
                SetupScreen(pkgs = pkgs, presetType = type, onClose = { nav.popBackStack() })
            }
        }
    }
}
