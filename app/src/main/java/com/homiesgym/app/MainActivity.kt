package com.homiesgym.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homiesgym.app.billing.BillingManager
import com.homiesgym.app.data.AppState
import com.homiesgym.app.data.GymRepository
import com.homiesgym.app.data.Workout
import kotlinx.coroutines.launch

private val workouts = listOf(
    Workout("push", "Push Power", "Chest • Shoulders • Triceps", 42),
    Workout("pull", "Pull Strength", "Back • Biceps", 38),
    Workout("legs", "Leg Day", "Quads • Hamstrings • Glutes", 48),
    Workout("hiit", "HIIT Burner", "Full Body • Cardio", 25, true),
    Workout("mobility", "Athletic Mobility", "Mobility • Recovery", 20, true)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomiesApp() }
    }
}

@Composable
private fun HomiesApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { GymRepository(context) }
    val state by repo.state.collectAsState(initial = AppState())
    val billing = remember { BillingManager(context) }
    val premium by billing.premium.collectAsState()
    var selected by remember { mutableStateOf<Workout?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { billing.connect() }

    MaterialTheme {
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Homies Gym", style = MaterialTheme.typography.headlineLarge)
                    Text("Train together. Get stronger together.")
                    Spacer(Modifier.height(8.dp))
                    Text("Streak: " + state.streak + " days  •  Workouts: " + state.workouts + "  •  Minutes: " + state.minutes)
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Today's Mission", style = MaterialTheme.typography.titleLarge)
                            Text("Build a stronger upper body")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { selected = workouts.first() }) {
                                Text("START SESSION")
                            }
                        }
                    }
                }
                item { Text("Training", style = MaterialTheme.typography.headlineSmall) }
                items(workouts) { workout ->
                    Card(Modifier.fillMaxWidth(), onClick = { selected = workout }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            Text(workout.muscle)
                            Text(workout.minutes.toString() + " minutes")
                            if (workout.premium && !premium) Text("Homies Pro")
                        }
                    }
                }
                if (!premium) {
                    item {
                        Button(
                            onClick = { billing.launch(context as Activity) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("UPGRADE TO HOMIES PRO") }
                    }
                }
            }
        }
    }

    selected?.let { workout ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(workout.name) },
            text = { Text(workout.muscle + "\n" + workout.minutes + " minutes") },
            confirmButton = {
                Button(
                    enabled = !workout.premium || premium,
                    onClick = {
                        scope.launch { repo.complete(workout.minutes) }
                        selected = null
                    }
                ) { Text("COMPLETE") }
            },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("CANCEL") } }
        )
    }
}
