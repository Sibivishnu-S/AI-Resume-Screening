package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AiAdvisorChatScreen
import com.example.ui.screens.AnalysisDetailReportScreen
import com.example.ui.screens.AtsAnalyzerScreen
import com.example.ui.screens.CandidateRankingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HrAnalyticsScreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryLight
import com.example.ui.viewmodel.AtsViewModel

sealed class NavDestination(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : NavDestination("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Analyzer : NavDestination("analyzer", "ATS Screening", Icons.Default.AutoAwesome)
    object Candidates : NavDestination("candidates", "Rankings", Icons.Default.Group)
    object Analytics : NavDestination("analytics", "HR Analytics", Icons.Default.Analytics)
    object AiAdvisor : NavDestination("ai_advisor", "AI Coach", Icons.Default.Psychology)
    object ReportDetail : NavDestination("report_detail", "Full Report", Icons.Default.AutoAwesome)
}

val navItems = listOf(
    NavDestination.Dashboard,
    NavDestination.Analyzer,
    NavDestination.Candidates,
    NavDestination.Analytics,
    NavDestination.AiAdvisor
)

@Composable
fun AtsAppNavigation(viewModel: AtsViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.userMessageToast.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentRoute != NavDestination.ReportDetail.route) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) PrimaryLight else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavDestination.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAnalyzer = { navController.navigate(NavDestination.Analyzer.route) },
                    onNavigateToCandidates = { navController.navigate(NavDestination.Candidates.route) },
                    onNavigateToAnalytics = { navController.navigate(NavDestination.Analytics.route) },
                    onNavigateToChat = { navController.navigate(NavDestination.AiAdvisor.route) }
                )
            }

            composable(NavDestination.Analyzer.route) {
                AtsAnalyzerScreen(
                    viewModel = viewModel,
                    onNavigateToReport = { navController.navigate(NavDestination.ReportDetail.route) }
                )
            }

            composable(NavDestination.Candidates.route) {
                CandidateRankingScreen(viewModel = viewModel)
            }

            composable(NavDestination.Analytics.route) {
                HrAnalyticsScreen(viewModel = viewModel)
            }

            composable(NavDestination.AiAdvisor.route) {
                AiAdvisorChatScreen(viewModel = viewModel)
            }

            composable(NavDestination.ReportDetail.route) {
                AnalysisDetailReportScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
