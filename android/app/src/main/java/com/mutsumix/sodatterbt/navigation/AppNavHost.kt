package com.mutsumix.sodatterbt.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.mutsumix.sodatterbt.ui.detail.DetailScreen
import com.mutsumix.sodatterbt.ui.harvest.HarvestScreen
import com.mutsumix.sodatterbt.ui.history.HistoryScreen
import com.mutsumix.sodatterbt.ui.home.HomeScreen
import com.mutsumix.sodatterbt.ui.labelprint.LabelPrintScreen
import com.mutsumix.sodatterbt.ui.photorecord.PhotoRecordScreen
import com.mutsumix.sodatterbt.ui.qrscan.QrScanScreen
import com.mutsumix.sodatterbt.ui.seeding.SeedingScreen
import com.mutsumix.sodatterbt.ui.settings.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // アプリがバックグラウンドにある状態でディープリンクが来た場合にNavControllerへ転送
    val context = LocalContext.current
    DisposableEffect(navController) {
        val activity = context as? androidx.activity.ComponentActivity
        val listener = Consumer<android.content.Intent> { intent ->
            navController.handleDeepLink(intent)
        }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }

    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        dest.hasRoute(Home::class) ||
            dest.hasRoute(History::class) ||
            dest.hasRoute(Settings::class)
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(navController, currentDestination)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Home,
        ) {
            composable<Home> {
                HomeScreen(
                    innerPadding = innerPadding,
                    onDeviceClick = { deviceId -> navController.navigate(Detail(deviceId)) },
                    onEmptySlotClick = { deviceId -> navController.navigate(Seeding(deviceId)) },
                    onQrScanClick = { navController.navigate(QrScan()) },
                )
            }
            composable<Seeding> { backStackEntry ->
                val route: Seeding = backStackEntry.toRoute()
                SeedingScreen(
                    deviceId = route.deviceId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable<Detail> { backStackEntry ->
                val route: Detail = backStackEntry.toRoute()
                DetailScreen(
                    deviceId = route.deviceId,
                    promptCamera = route.promptCamera,
                    onBack = { navController.popBackStack() },
                    onHarvestClick = { navController.navigate(Harvest(route.deviceId)) },
                    onPhotoClick = { navController.navigate(PhotoRecord(route.deviceId)) },
                    onDeleted = { navController.popBackStack() },
                )
            }
            composable<QrScan>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "sodatterbt://cultivation/{cultivationId}" }
                )
            ) { backStackEntry ->
                val route: QrScan = backStackEntry.toRoute()
                QrScanScreen(
                    cultivationId = route.cultivationId,
                    onNavigateToPhotoRecord = { deviceId ->
                        navController.navigate(Detail(deviceId, promptCamera = true)) {
                            popUpTo<QrScan> { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<PhotoRecord> { backStackEntry ->
                val route: PhotoRecord = backStackEntry.toRoute()
                PhotoRecordScreen(
                    deviceId = route.deviceId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable<Harvest> { backStackEntry ->
                val route: Harvest = backStackEntry.toRoute()
                HarvestScreen(
                    deviceId = route.deviceId,
                    onBack = { navController.popBackStack() },
                    onLabelPrintClick = { weight -> navController.navigate(LabelPrint(route.deviceId, weight)) },
                    onComplete = {
                        navController.navigate(Home) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    },
                )
            }
            composable<LabelPrint> { backStackEntry ->
                val route: LabelPrint = backStackEntry.toRoute()
                LabelPrintScreen(
                    deviceId = route.deviceId,
                    onBack = { navController.popBackStack() },
                    onDone = {
                        navController.navigate(Home) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    },
                )
            }
            composable<History> {
                HistoryScreen(
                    innerPadding = innerPadding,
                    onRecordClick = { deviceId, cultivationId ->
                        navController.navigate(Detail(deviceId, cultivationId = cultivationId))
                    },
                )
            }
            composable<Settings> {
                SettingsScreen(innerPadding = innerPadding)
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    navController: NavController,
    currentDestination: NavDestination?,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute(Home::class) } == true,
            onClick = { navController.navigateTopLevel(Home) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("ホーム") },
        )
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute(History::class) } == true,
            onClick = { navController.navigateTopLevel(History) },
            icon = { Icon(Icons.Filled.History, contentDescription = null) },
            label = { Text("履歴") },
        )
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute(Settings::class) } == true,
            onClick = { navController.navigateTopLevel(Settings) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("設定") },
        )
    }
}

private fun NavController.navigateTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
