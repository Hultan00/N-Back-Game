package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.TextToSpeechManager
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository


/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>

    val nBack: StateFlow<Int>
    val gridType: StateFlow<GridType>
    val blinkDuration: StateFlow<Long>
    val eventInterval: StateFlow<Long>
    val numberOfEvents: StateFlow<Int>

    val isVisualClicked: StateFlow<Boolean>
    val isAudioClicked: StateFlow<Boolean>

    val smallBoxes: StateFlow<Boolean>

    fun setGameType(gameType: GameType)

    fun setNBackValue(value: Int)

    fun setNumOfEvents(value: Int)
    fun setBlinkDuration(value: Long)
    fun setEventInterval(value: Long)
    fun setGridType(value: Int)
    fun setSmallBoxes(boolean: Boolean)

    fun setVisualClicked(boolean: Boolean)
    fun setAudioClicked(boolean: Boolean)

    fun resetHighscore()

    fun startGame()
    fun stopGame()

    fun checkMatch()
    fun getScore():Int
    fun checkGuess():Boolean

    fun setTextToSpeech(t2s: TextToSpeechManager)

    fun savePreferences()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private var t2s: TextToSpeechManager? = null

    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val _nBack = MutableStateFlow(1)
    override val nBack: StateFlow<Int>
        get() = _nBack

    private val _gridType = MutableStateFlow(GridType.Grid_3x3)
    override val gridType: StateFlow<GridType>
        get() = _gridType

    private val _isVisualClicked = MutableStateFlow(false)
    override val isVisualClicked: StateFlow<Boolean>
        get() = _isVisualClicked

    private val _isAudioClicked = MutableStateFlow(false)
    override val isAudioClicked: StateFlow<Boolean>
        get() = _isAudioClicked

    private val _blinkDuration = MutableStateFlow(1000L)
    override val blinkDuration: StateFlow<Long>
        get() = _blinkDuration

    private val _eventInterval = MutableStateFlow(2000L)
    override val eventInterval: StateFlow<Long>
        get() = _eventInterval

    private val _numberOfEvents = MutableStateFlow(10)
    override val numberOfEvents: StateFlow<Int>
        get() = _numberOfEvents

    private val _smallBoxes = MutableStateFlow(false)
    override val smallBoxes: MutableStateFlow<Boolean>
        get() = _smallBoxes


    private var job: Job? = null  // coroutine job for the game event

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
        Log.d("GameVM", "Changed gameState value: ${_gameState.value}")
    }

    override fun setNBackValue(value: Int) {
        _nBack.value = value
        Log.d("GameVM", "Changed nBack value: ${_nBack.value}")
    }

    override fun setBlinkDuration(value: Long){
        _blinkDuration.value = value
        Log.d("GameVM", "Changed blinkDuration value: ${_blinkDuration.value}")
    }

    override fun setEventInterval(value: Long){
        _eventInterval.value = value
        Log.d("GameVM", "Changed EventInterval value: ${_blinkDuration.value}")
    }

    override fun setGridType(value: Int){
        _gridType.value = getGridType(value)
        Log.d("GameVM", "Changed gridType value: ${_gridType.value}")
    }

    override fun setNumOfEvents(value: Int) {
        _numberOfEvents.value = value
        Log.d("GameVM", "Changed NumberOfEvents value: ${_numberOfEvents.value}")
    }

    override fun savePreferences(){
        viewModelScope.launch {
            userPreferencesRepository.saveNBack(_nBack.value)
        }
        viewModelScope.launch {
            userPreferencesRepository.saveBlinkDuration(_blinkDuration.value)
        }
        viewModelScope.launch {
            userPreferencesRepository.saveEventInterval(_eventInterval.value)
        }
        viewModelScope.launch {
            userPreferencesRepository.saveGridType(_gridType.value)
        }
        viewModelScope.launch {
            userPreferencesRepository.saveNumberOfEvents(_numberOfEvents.value)
        }
        Log.d("GameVM", "Saved Preferences!")
    }

    override fun setVisualClicked(boolean: Boolean){
        _isVisualClicked.value = boolean
    }

    override fun setAudioClicked(boolean: Boolean){
        _isAudioClicked.value = boolean
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        _gameState.value = _gameState.value.copy(isRunning = true)
        _isVisualClicked.value = false
        _isAudioClicked.value = false
        _score.value = 0

        Log.d("GameVM", "Loading values...")
        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(_numberOfEvents.value, _gridType.value.size*_gridType.value.size, 30, nBack.value).toList().toTypedArray()
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(events)
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
            _gameState.value = _gameState.value.copy(isRunning = false)
            // Todo: update the highscore
            updateHighscore()

        }
    }

    override fun stopGame(){
        job?.cancel()
        _gameState.value = _gameState.value.copy(isRunning = false)
        _score.value = 0
        _gameState.value = _gameState.value.copy(eventValue = -1)
        _gameState.value = _gameState.value.copy(eventValueIndex = 0)
        _isVisualClicked.value = false
        _isAudioClicked.value = false
    }

    override fun checkMatch() {
        /**
         * Todo: This function should check if there is a match when the user presses a match button
         * Make sure the user can only register a match once for each event.
         */
        if(!_isVisualClicked.value && _gameState.value.isRunning) {
            Log.d("GameVM", "1 ${_gameState.value.eventValueIndex} >= ${_nBack.value}: ${_gameState.value.eventValueIndex >= _nBack.value}")
            if ((_gameState.value.eventValueIndex >= _nBack.value)) {
                Log.d("GameVM", "2 ${events[_gameState.value.eventValueIndex - _nBack.value]} == ${events[_gameState.value.eventValueIndex]}: ${events[_gameState.value.eventValueIndex - _nBack.value] == events[_gameState.value.eventValueIndex]}")
                if (events[_gameState.value.eventValueIndex - _nBack.value] == events[_gameState.value.eventValueIndex]) {
                    _score.value = _score.value + 1
                }else{
                    _score.value = _score.value - 1
                }
            }else{
                _score.value = _score.value - 1
            }
            Log.d("GameVM", "Score: ${_score.value}")
        }
    }

    override fun checkGuess():Boolean {
        /**
         * Todo: This function should check if there is a match when the user presses a match button
         * Make sure the user can only register a match once for each event.
         */
        if(_gameState.value.isRunning) {
            Log.d("GameVM", "1 ${_gameState.value.eventValueIndex} >= ${_nBack.value}: ${_gameState.value.eventValueIndex >= _nBack.value}")
            return if ((_gameState.value.eventValueIndex >= _nBack.value)) {
                Log.d("GameVM", "2 ${events[_gameState.value.eventValueIndex - _nBack.value]} == ${events[_gameState.value.eventValueIndex]}: ${events[_gameState.value.eventValueIndex - _nBack.value] == events[_gameState.value.eventValueIndex]}")
                events[_gameState.value.eventValueIndex - _nBack.value] == events[_gameState.value.eventValueIndex]
            }else{
                false
            }
        }
        return false
    }

    override fun setTextToSpeech(t2s: TextToSpeechManager) {
        this.t2s = t2s
    }

    override fun setSmallBoxes(boolean: Boolean){
        _smallBoxes.value = boolean
    }

    private fun updateHighscore(){
        var matchesFloat = (_numberOfEvents.value * (30.0 / 100.0))
        var matches = matchesFloat.toInt()
        Log.d("GameVM", "Matches: ${matches}")
        var successrate = 0.0
        if(_score.value != 0) {
            Log.d("GameVM", "Score: ${_score.value}")
            successrate = (_score.value.toDouble() / matches.toDouble()) * 100.0
        }

        if(successrate.toInt() > _highscore.value) {
            Log.d("GameVM", "SuccessRate: $successrate")
            _highscore.value = successrate.toInt()
            Log.d("GameVM", "HS: ${matches}")

            viewModelScope.launch {
                userPreferencesRepository.saveHighScore(_highscore.value)
            }
        }
    }

    override fun getScore():Int{
        var matchesFloat = (_numberOfEvents.value * (30.0 / 100.0))
        var matches = matchesFloat.toInt()
        Log.d("GameVM", "Matches: ${matches}")
        var successrate = 0.0
        if(_score.value != 0) {
            Log.d("GameVM", "Score: ${_score.value}")
            successrate = (_score.value.toDouble() / matches.toDouble()) * 100.0
        }
        return successrate.toInt()
    }

    override fun resetHighscore(){
        _highscore.value = 0
        viewModelScope.launch {
            userPreferencesRepository.saveHighScore(_highscore.value)
        }
    }

    private suspend fun runAudioGame(events: Array<Int>) {
        // Todo: Make work for Basic grade
        var index = 0;
        _gameState.value = _gameState.value.copy(eventValueIndex = index)
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValue = value)
            t2s?.speak(t2s?.num2Char(_gameState.value.eventValue).toString())
            delay(_blinkDuration.value)
            _gameState.value = _gameState.value.copy(eventValue = -1)
            delay(_eventInterval.value - _blinkDuration.value)
            setVisualClicked(false)
            setAudioClicked(false)
            index += 1
            _gameState.value = _gameState.value.copy(eventValueIndex = index)
        }
    }

    private suspend fun runVisualGame(events: Array<Int>){
        // Todo: Replace this code for actual game code
        var index = 0;
        _gameState.value = _gameState.value.copy(eventValueIndex = index)
        for (value in events) {
            _gameState.value = _gameState.value.copy(eventValue = value)
            delay(_blinkDuration.value)
            _gameState.value = _gameState.value.copy(eventValue = -1)
            delay(_eventInterval.value - _blinkDuration.value)
            setVisualClicked(false)
            setAudioClicked(false)
            index += 1
            _gameState.value = _gameState.value.copy(eventValueIndex = index)
        }

    }

    private fun runAudioVisualGame(){
        // Todo: Make work for Higher grade
    }

    private fun getGridType(value: Int):GridType{
        when (value){
            2 -> return GridType.Grid_2x2
            3 -> return GridType.Grid_3x3
            4 -> return GridType.Grid_4x4
            5 -> return GridType.Grid_5x5
        }
        return GridType.Grid_3x3
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.nBack.collect {
                _nBack.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.blinkDuration.collect {
                _blinkDuration.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.eventInterval.collect {
                _eventInterval.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.gridType.collect {
                _gridType.value = GridType.valueOf(it)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.numberOfEvents.collect {
                _numberOfEvents.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

// Class with the different game types
enum class GridType(val size: Int){
    Grid_2x2(2),
    Grid_3x3(3),
    Grid_4x4(4),
    Grid_5x5(5)
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val isRunning: Boolean = false,
    val eventValue: Int = -1,  // The value of the array string
    val eventValueIndex: Int = 0 // Current eventValueIndex

)

