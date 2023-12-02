package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.ui.theme.StandbyBlue
import mobappdev.example.nback_cimpl.ui.theme.StyleBlue
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@Composable
fun SettingsScreen(
    vm: GameViewModel,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }
    val nBack by vm.nBack.collectAsState()
    val blinkDuration by vm.blinkDuration.collectAsState()
    val eventInterval by vm.eventInterval.collectAsState()
    val numberOfEvents by vm.numberOfEvents.collectAsState()
    val gridType by vm.gridType.collectAsState()
    val smallBoxes by vm.smallBoxes.collectAsState()

    var isSettingsChanged by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Game Settings",
                style = MaterialTheme.typography.headlineLarge
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(1) {
                    Text(
                        text = "N-Back Value: $nBack",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                    ) {
                        val itemCount = 50
                        val startIndex = 1

                        items(itemCount) { index ->
                            val value = startIndex + index
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(70.dp)
                                    .background(
                                        color = if (value == nBack) StyleBlue else StandbyBlue,
                                        shape = RoundedCornerShape(50.dp) // Set the same corner radius as above
                                    )
                                    .padding(8.dp)
                                    .clickable {
                                        if (value >= numberOfEvents) {
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = "Criteria: N-Back < Number of Events",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            vm.setNBackValue(value)
                                            isSettingsChanged = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$value",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = "Grid Type: ${gridType.size}x${gridType.size}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                    ) {
                        val itemCount = 4
                        val startIndex = 2

                        items(itemCount) { index ->
                            val value = startIndex + index
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(70.dp)
                                    .background(
                                        color = if (value == gridType.size) StyleBlue else StandbyBlue,
                                        shape = RoundedCornerShape(50.dp) // Set the same corner radius as above
                                    )
                                    .padding(8.dp)
                                    .clickable {
                                        vm.setGridType(value)
                                        isSettingsChanged = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${value}x${value}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = "Number Of Events: $numberOfEvents",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                    ) {
                        val itemCount = 99
                        val startIndex = 2

                        items(itemCount) { index ->
                            val value = startIndex + index
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(70.dp)
                                    .background(
                                        color = if (value == numberOfEvents) StyleBlue else StandbyBlue,
                                        shape = RoundedCornerShape(50.dp) // Set the same corner radius as above
                                    )
                                    .padding(8.dp)
                                    .clickable {
                                        if (value <= nBack) {
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = "Criteria: Number of Events > N-Back",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            vm.setNumOfEvents(value)
                                            isSettingsChanged = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$value",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = "Event Interval [S]: ${(eventInterval.toFloat()/1000.0)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                    ) {
                        val itemCount = 17
                        val startIndex = 2

                        items(itemCount) { index ->
                            val value = (startIndex + index*0.5)*500
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(70.dp)
                                    .background(
                                        color = if (value.toLong() == eventInterval) StyleBlue else StandbyBlue,
                                        shape = RoundedCornerShape(50.dp) // Set the same corner radius as above
                                    )
                                    .padding(8.dp)
                                    .clickable {
                                        if (value < blinkDuration) {
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = "Criteria: EventInterval > BlinkDuration",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            vm.setEventInterval(value.toLong())
                                            isSettingsChanged = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${value/1000}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = "Blink Duration [S]: ${(blinkDuration.toFloat()/1000.0)}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                    ) {
                        val itemCount = 10
                        val startIndex = 0.5

                        items(itemCount) { index ->
                            val value = (startIndex + index*0.5)*500
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(70.dp)
                                    .background(
                                        color = if (value.toLong() == blinkDuration) StyleBlue else StandbyBlue,
                                        shape = RoundedCornerShape(50.dp) // Set the same corner radius as above
                                    )
                                    .padding(8.dp)
                                    .clickable {
                                        if (value > eventInterval) {
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = "Criteria: BlinkDuration < EventInterval",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            vm.setBlinkDuration(value.toLong())
                                            isSettingsChanged = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${value/1000}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                    Text(
                        text = "Small Boxes: $smallBoxes",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                    ) {
                        val itemCount = 2
                        val startIndex = 0

                        items(itemCount) { index ->
                            val value = startIndex + index
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(70.dp)
                                    .background(
                                        color = if (intToBoolean(value) == smallBoxes) StyleBlue else StandbyBlue,
                                        shape = RoundedCornerShape(50.dp) // Set the same corner radius as above
                                    )
                                    .padding(8.dp)
                                    .clickable {
                                        vm.setSmallBoxes(intToBoolean(value))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${intToBoolean(value)}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                // Todo: change this button behaviour
                                navController.navigate("home")
                                vm.savePreferences()
                                if(isSettingsChanged){
                                    vm.resetHighscore()
                                }
                            },
                            modifier = Modifier
                                .width(110.dp)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                        Button(
                            onClick = {
                                // Todo: change this button behaviour
                                vm.setNBackValue(2)
                                vm.setGridType(3)
                                vm.setNumOfEvents(10)
                                vm.setEventInterval(2000L)
                                vm.setBlinkDuration(1000L)
                                isSettingsChanged = true
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

fun intToBoolean(value: Int): Boolean {
    return value != 0
}