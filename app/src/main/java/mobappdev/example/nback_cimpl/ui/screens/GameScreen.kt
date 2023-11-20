package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.TextToSpeechManager
import mobappdev.example.nback_cimpl.ui.theme.BlinkBlue
import mobappdev.example.nback_cimpl.ui.theme.StandbyBlue
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@Composable
fun GameScreen(
    vm: GameViewModel,
    navController: NavHostController,
    t2s: TextToSpeechManager
) {
    val score by vm.score.collectAsState()
    val gameState by vm.gameState.collectAsState()

    val nBack by vm.nBack.collectAsState()
    val gridType by vm.gridType.collectAsState()
    val numberOfEvents by vm.numberOfEvents.collectAsState()

    val isVisualClicked by vm.isVisualClicked.collectAsState()
    val isAudioClicked by vm.isAudioClicked.collectAsState()

    val smallBoxes by vm.smallBoxes.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var gameStr by remember {
        if(gameState.isRunning) mutableStateOf("Stop Game") else mutableStateOf("Start Game")
    }
    var progress by remember { mutableStateOf(0.5f) } // Change the initial progress as needed

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    if(isLandscape){
        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Todo: change this button behaviour
                                    navController.navigate("home")
                                },
                                enabled = gameStr.equals("Start Game"),
                                modifier = Modifier
                                    .size(70.dp)
                                    .align(Alignment.Center)
                            ) {
                                Text(
                                    text = "<",
                                    style = MaterialTheme.typography.displaySmall,
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.padding(32.dp),
                            text = "S = $score",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Button(
                            onClick = {
                                // Todo: change this button behaviour
                                vm.checkMatch()
                                vm.setAudioClicked(true)
                            },
                            enabled = ((gameState.gameType == GameType.Audio) || (gameState.gameType == GameType.AudioVisual)),
                            colors = ButtonDefaults.buttonColors(
                                contentColor = if (isAudioClicked && vm.checkGuess()) Color.Green else if(isAudioClicked && !vm.checkGuess()) Color.Red else Color.White)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.note),
                                contentDescription = "Sound",
                                modifier = Modifier
                                    .height(48.dp)
                                    .aspectRatio(3f / 2f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (col in 0 until gridType.size){
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding((10 / gridType.size).dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (row in 1..gridType.size) {
                                        Box(
                                            modifier = Modifier
                                                .size(if(!smallBoxes) (300 / gridType.size).dp else (200 / gridType.size).dp)
                                                .background(
                                                    if ((gameState.eventValue == (col * gridType.size + row)) ||
                                                        ((gameState.gameType == GameType.Audio) && (gameState.eventValue > -1))
                                                    )
                                                        BlinkBlue else StandbyBlue,
                                                    shape = MaterialTheme.shapes.medium
                                                )
                                        ){}
                                    }
                                }
                            }
                        }
                    }
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        progress = gameState.eventValueIndex / numberOfEvents.toFloat()

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = progress,
                                color = BlinkBlue,
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(4.dp) // Adjust padding to control the position
                                    .align(Alignment.TopCenter),
                                strokeWidth = 12.dp, // Adjust the strokeWidth as needed
                            )

                            Button(
                                onClick = {
                                    if (gameStr.equals("Start Game")) {
                                        vm.startGame()
                                        gameStr = "Reset Game"
                                    } else {
                                        progress = 0.0F
                                        if(!gameState.isRunning) {
                                            var score = vm.getScore()
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    message = "SuccessRate: $score%",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                        vm.stopGame()
                                        gameStr = "Start Game"
                                    }
                                },
                                modifier = Modifier
                                    .size(70.dp)
                                    .align(Alignment.Center)
                            ) {
                                // Progress Indicator
                                Icon(
                                    painter = painterResource(if (gameStr == "Start Game") R.drawable.play else R.drawable.pause),
                                    contentDescription = "Play",
                                    modifier = Modifier
                                        .height(70.dp)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.padding(32.dp),
                            text = "N = $nBack",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Button(
                            onClick = {
                                // Todo: change this button behaviour
                                vm.checkMatch()
                                vm.setVisualClicked(true)
                            },
                            enabled = ((gameState.gameType == GameType.Visual) || (gameState.gameType == GameType.AudioVisual)),
                            colors = ButtonDefaults.buttonColors(
                                contentColor = if (isVisualClicked && vm.checkGuess()) Color.Green else if(isVisualClicked && !vm.checkGuess()) Color.Red else Color.White)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_square),
                                contentDescription = "Visual",
                                modifier = Modifier
                                    .height(48.dp)
                                    .aspectRatio(3f / 2f)
                            )
                        }
                    }
                }
            }
        }
    }
    else{
        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Button(
                        onClick = {
                            // Todo: change this button behaviour
                            navController.navigate("home")
                        },
                        enabled = gameStr.equals("Start Game"),
                        modifier = Modifier
                            .width(200.dp)
                            .height(70.dp),
                    ) {
                        Text(
                            text = "<Menu",
                            style = MaterialTheme.typography.displaySmall,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(5.dp),
                        text = "N = $nBack",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                // Todo: You'll probably want to change this "BOX" part of the composable
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        for (col in 0 until gridType.size){
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding((10 / gridType.size).dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (row in 1..gridType.size) {
                                    Box(
                                        modifier = Modifier
                                            .size(if(!smallBoxes) (300 / gridType.size).dp else (200 / gridType.size).dp)
                                            .background(
                                                if ((gameState.eventValue == (col * gridType.size + row)) ||
                                                    ((gameState.gameType == GameType.Audio) && (gameState.eventValue > -1))
                                                )
                                                    BlinkBlue else StandbyBlue,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                    ){}
                                }
                            }
                        }
                        progress = gameState.eventValueIndex / numberOfEvents.toFloat()

                        LinearProgressIndicator(
                            progress = progress,
                            color = BlinkBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Current score is: $score",
                            textAlign = TextAlign.Center
                        )

                    }
                }
                Button(onClick = {
                    if(gameStr.equals("Start Game")){
                        vm.startGame()
                        gameStr = "Reset Game"
                    }else{
                        progress = 0.0F
                        if(!gameState.isRunning) {
                            var score = vm.getScore()
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = "SuccessRate: $score%",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        vm.stopGame()
                        gameStr = "Start Game"
                    }
                },
                    modifier = Modifier
                        .width(300.dp)
                        .height(70.dp)
                ) {
                    Text(
                        text = gameStr,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            // Todo: change this button behaviour
                            vm.checkMatch()
                            vm.setAudioClicked(true)
                        },
                        enabled = ((gameState.gameType == GameType.Audio) || (gameState.gameType == GameType.AudioVisual)),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = if (isAudioClicked && vm.checkGuess()) Color.Green else if(isAudioClicked && !vm.checkGuess()) Color.Red else Color.White)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.note),
                            contentDescription = "Sound",
                            modifier = Modifier
                                .height(48.dp)
                                .aspectRatio(3f / 2f)
                        )
                    }
                    Button(
                        onClick = {
                            // Todo: change this button behaviour
                            vm.checkMatch()
                            vm.setVisualClicked(true)
                        },
                        enabled = ((gameState.gameType == GameType.Visual) || (gameState.gameType == GameType.AudioVisual)),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = if (isVisualClicked && vm.checkGuess()) Color.Green else if(isVisualClicked && !vm.checkGuess()) Color.Red else Color.White)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_square),
                            contentDescription = "Visual",
                            modifier = Modifier
                                .height(48.dp)
                                .aspectRatio(3f / 2f)
                        )
                    }
                }
            }
        }
    }

}