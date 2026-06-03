package com.rustcrawlercoach.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rustcrawlercoach.ui.screens.DashboardScreen
import com.rustcrawlercoach.ui.screens.EditorScreen
import com.rustcrawlercoach.ui.screens.FeynmanScreen
import com.rustcrawlercoach.ui.screens.LearningScreen
import com.rustcrawlercoach.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Learning : Screen("learning/{chapterId}") {
        fun createRoute(chapterId: Int) = "learning/$chapterId"
    }
    data object Editor : Screen("editor/{chapterId}") {
        fun createRoute(chapterId: Int) = "editor/$chapterId"
    }
    data object Feynman : Screen("feynman/{chapterId}") {
        fun createRoute(chapterId: Int) = "feynman/$chapterId"
    }
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToLearning = { chapterId ->
                    navController.navigate(Screen.Learning.createRoute(chapterId))
                },
                onNavigateToFeynman = { chapterId ->
                    navController.navigate(Screen.Feynman.createRoute(chapterId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Learning.route,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
            LearningScreen(
                chapterId = chapterId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { id ->
                    navController.navigate(Screen.Editor.createRoute(id))
                },
                onNavigateToFeynman = { id ->
                    navController.navigate(Screen.Feynman.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
            EditorScreen(
                chapterId = chapterId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Feynman.route,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
            FeynmanScreen(
                chapterId = chapterId,
                onNavigateBack = { navController.popBackStack() },
                onChapterCompleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
