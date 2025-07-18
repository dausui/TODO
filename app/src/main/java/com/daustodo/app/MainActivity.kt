package com.daustodo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daustodo.app.ui.screens.pomodoro.PomodoroScreen
import com.daustodo.app.ui.screens.todo.AddEditTaskScreen
import com.daustodo.app.ui.screens.todo.TodoScreen
import com.daustodo.app.ui.theme.DausTodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DausTodoTheme {
                DausTodoApp()
            }
        }
    }
}

@Composable
fun DausTodoApp() {
    val navController = rememberNavController()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        DausTodoNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun DausTodoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "todo",
        modifier = modifier
    ) {
        composable("todo") {
            TodoScreen(
                onNavigateToAddTask = {
                    navController.navigate("add_task")
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate("edit_task/$taskId")
                },
                onNavigateToPomodoro = { taskId ->
                    if (taskId != null) {
                        navController.navigate("pomodoro/$taskId")
                    } else {
                        navController.navigate("pomodoro")
                    }
                }
            )
        }
        
        composable("add_task") {
            AddEditTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
            if (taskId != null) {
                AddEditTaskScreen(
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable("pomodoro") {
            PomodoroScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("pomodoro/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
            PomodoroScreen(
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}