package com.cericatto.rockwooddial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cericatto.rockwooddial.ui.navigation.NavHostComposable
import com.cericatto.rockwooddial.ui.theme.RockWoodDialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			RockWoodDialTheme(darkTheme = true) {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = Color.Black,
				) {
					NavHostComposable()
				}
			}
		}
	}
}
