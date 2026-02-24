package com.cericatto.rockwooddial.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cericatto.rockwooddial.ui.main_screen.MainScreenRoot

@Composable
fun NavHostComposable() {
	val navController = rememberNavController()
	NavHost(
		navController = navController,
		startDestination = Route.MainScreen
	) {
		composable<Route.MainScreen> {
			MainScreenRoot()
		}
	}
}
