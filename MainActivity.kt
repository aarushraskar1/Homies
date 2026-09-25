package com.homiesgym.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homiesgym.app.billing.BillingManager
import com.homiesgym.app.data.*
import kotlinx.coroutines.launch

private val workouts = listOf(
    Workout("push", "Push Power", "Chest • Shoulders • Triceps", 42),
    Workout("pull", "Pull Strength", "Back • Biceps", 38),
    Workout("legs", "Leg Day", "Quads • Hamstrings • Glutes", 48),
    Workout("hiit", "HIIT Burner", "Full Body • Cardio", 25, true),
    Workout("mobility", "Athletic Mobility", "Mobility • Recovery", 20, true)
)

class GymViewModel(private val repo: GymRepository) : ViewModel() {
    val state = repo.state
    fun complete(w: Workout) { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { repo.complete(w.minutes) } }
    fun unlock() { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { repo.setPremium(true) } }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { HomiesApp() } }
}

@Composable fun HomiesApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { GymRepository(context) }
    val vm: GymViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory { override fun <T : ViewModel> create(c: Class<T>): T = GymViewModel(repo) as T })
    val state by vm.state.collectAsStateWithLifecycle(AppState())
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Workout?>(null) }
    val billing = remember { BillingManager(context) }
    LaunchedEffect(Unit) { billing.connect() }
    val isPremium = state.premium || billing.premium.collectAsState().value

    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFB7F36B), background = Color(0xFF0D0E10), surface = Color(0xFF17191D))) {
        Scaffold(bottomBar = { NavigationBar { listOf("Home" to Icons.Default.Home, "Train" to Icons.Default.FitnessCenter, "Progress" to Icons.Default.Insights, "Profile" to Icons.Default.Person).forEachIndexed { i, p -> NavigationBarItem(tab == i, { tab = i }, icon = { Icon(p.second, null) }, label = { Text(p.first) }) } } }) { pad ->
            when (tab) {
                0 -> HomeScreen(state, isPremium, { tab = 1 }, { selected = it })
                1 -> TrainScreen(isPremium, { selected = it })
                2 -> ProgressScreen(state)
                else -> ProfileScreen(isPremium, onUpgrade = { billing.launch(context as Activity) })
            }
        }
        selected?.let { w -> WorkoutDialog(w, isPremium, { vm.complete(w); selected = null }, { selected = null }) }
    }
}

@Composable fun Header(title: String, subtitle: String? = null) { Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) { Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); subtitle?.let { Text(it, color = Color.Gray) } } }

@Composable fun HomeScreen(state: AppState, premium: Boolean, onTrain: () -> Unit, onWorkout: (Workout) -> Unit) { LazyColumn(Modifier.fillMaxSize()) {
    item { Header("Homies Gym", "Train together. Get stronger together.") }
    item { Card(Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp)) { Text("TODAY'S MISSION", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("Build a stronger upper body", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("42 min • Intermediate", color = Color.Gray); Spacer(Modifier.height(16.dp)); Button(onClick = onTrain, modifier = Modifier.fillMaxWidth()) { Text("START SESSION") } } } }
    item { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { StatCard("${state.streak}", "DAY STREAK", Modifier.weight(1f)); StatCard("${state.workouts}", "WORKOUTS", Modifier.weight(1f)); StatCard("${state.minutes}", "MINUTES", Modifier.weight(1f)) } }
    item { Text("Recommended", Modifier.padding(20.dp, 8.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
    items(workouts.take(3)) { WorkoutRow(it, premium, onWorkout) }
} }

@Composable fun StatCard(v: String, l: String, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(14.dp)) { Text(v, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(l, style = MaterialTheme.typography.labelSmall, color = Color.Gray) } } }

@Composable fun TrainScreen(premium: Boolean, onWorkout: (Workout) -> Unit) { LazyColumn(Modifier.fillMaxSize()) { item { Header("Train", "Programs built around your goals") }; items(workouts) { WorkoutRow(it, premium, onWorkout) } } }

@Composable fun WorkoutRow(w: Workout, premium: Boolean, onClick: (Workout) -> Unit) { Card(Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(), onClick = { onClick(w) }) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary.copy(alpha=.16f), RoundedCornerShape(16.dp)), Alignment.Center) { Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(w.name, fontWeight = FontWeight.Bold); Text(w.muscle, color = Color.Gray); Text("${w.minutes} min", color = Color.Gray) }; if (w.premium && !premium) Icon(Icons.Default.Lock, "Premium") } } }

@Composable fun ProgressScreen(state: AppState) { Column(Modifier.fillMaxSize()) { Header("Progress", "Your consistency is the superpower") ; Card(Modifier.padding(16.dp).fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Consistency", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(18.dp)); LinearProgressIndicator({ (state.streak.coerceAtMost(7) / 7f) }, Modifier.fillMaxWidth()); Spacer(Modifier.height(10.dp)); Text("${state.streak}/7 days this week") } }; Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { StatCard("${state.workouts}", "TOTAL WORKOUTS", Modifier.weight(1f)); StatCard("${state.minutes}", "TOTAL MINUTES", Modifier.weight(1f)) } } }

@Composable fun ProfileScreen(premium: Boolean, onUpgrade: () -> Unit) { Column(Modifier.fillMaxSize()) { Header("Profile", "Your Homies account"); Card(Modifier.padding(16.dp).fillMaxWidth()) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Person, null, Modifier.size(48.dp)); Spacer(Modifier.width(16.dp)); Column { Text("Aarush", fontWeight = FontWeight.Bold); Text(if (premium) "HOMIES PRO MEMBER" else "FREE MEMBER", color = MaterialTheme.colorScheme.primary) } } }; if (!premium) Card(Modifier.padding(16.dp).fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Go Pro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Unlock premium programs, advanced analytics, challenges and future coaching tools.", color = Color.Gray); Spacer(Modifier.height(14.dp)); Button(onClick = onUpgrade, Modifier.fillMaxWidth()) { Text("UPGRADE TO HOMIES PRO") } } } else ListItem(headlineContent = { Text("Homies Pro active") }, supportingContent = { Text("Your premium training library is unlocked.") }, leadingContent = { Icon(Icons.Default.Verified, null) }) } }

@Composable fun WorkoutDialog(w: Workout, premium: Boolean, complete: () -> Unit, dismiss: () -> Unit) { AlertDialog(onDismissRequest = dismiss, title = { Text(w.name) }, text = { Column { Text(w.muscle); Text("${w.minutes} minutes"); if (w.premium && !premium) { Spacer(Modifier.height(12.dp)); Text("This is a Homies Pro workout.", color = MaterialTheme.colorScheme.primary) } } }, confirmButton = { Button(onClick = complete, enabled = !w.premium || premium) { Text("COMPLETE") } }, dismissButton = { TextButton(onClick = dismiss) { Text("CANCEL") } }) }
