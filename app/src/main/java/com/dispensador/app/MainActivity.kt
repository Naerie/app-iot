package com.dispensador.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dispensador.app.ui.*
import com.dispensador.app.ui.theme.DispensadorTheme
import com.dispensador.app.viewmodel.AuthState
import com.dispensador.app.viewmodel.AuthViewModel
import com.dispensador.app.viewmodel.DispenserViewModel

/**
 * MainActivity simplificada
 * Firebase se inicializa en DispensadorApplication
 */
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permiso de notificaciones otorgado")
        } else {
            Log.w(TAG, "Permiso de notificaciones denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity onCreate")

        // Solicitar permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            DispensadorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DispensadorApp()
                }
            }
        }
    }
}

@Composable
fun DispensadorApp() {
    val navController = rememberNavController()

    // ViewModels - Firebase ya está inicializado en Application
    val authViewModel: AuthViewModel = viewModel()
    val dispenserViewModel: DispenserViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()

    // Determinar la ruta inicial según el estado de autenticación
    val startDestination = when (authState) {
        is AuthState.Authenticated -> "home"
        is AuthState.Unauthenticated -> "login"
        else -> "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantalla de login
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de inicio
        composable("home") {
            HomeScreen(
                dispenserViewModel = dispenserViewModel,
                authViewModel = authViewModel,
                onNavigateToControl = {
                    navController.navigate("control")
                },
                onNavigateToSchedule = {
                    navController.navigate("schedule")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToNotifications = {
                    navController.navigate("notifications")
                }
            )
        }

        // Pantalla de control
        composable("control") {
            ControlScreen(
                dispenserViewModel = dispenserViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de horarios
        composable("schedule") {
            ScheduleScreen(
                dispenserViewModel = dispenserViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de historial
        composable("history") {
            HistoryScreen(
                dispenserViewModel = dispenserViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de notificaciones
        composable("notifications") {
            NotificationsScreen(
                dispenserViewModel = dispenserViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    // Observar cambios en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != "login") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route == "login") {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }
}
