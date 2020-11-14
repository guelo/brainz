package miguel.brainz2.ui.session

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import miguel.brainz2.grid.Entry
import miguel.brainz2.repos.MutableStats
import miguel.brainz2.modeRepo
import miguel.brainz2.repos.result
import miguel.brainz2.statsRepo
import kotlin.random.Random
import kotlin.random.nextInt

class SessionViewModel : ViewModel() {

    private val ticker = Ticker(100)

    private val mode = modeRepo.read()

    init {
        viewModelScope.launch {
            ticker.tick().collect {
                update()
            }
        }
    }


    val viewState =
        MutableLiveData(
            ViewState(
                GridState.Clear,
                InputState.NEUTRAL,
                InputState.NEUTRAL,
                "",
                "Dual ${mode.nback}-Back",
                "",
                null
            )
        )

    private fun emmit(
        gridState: GridState? = null,
        missedPosition: InputState? = null,
        missedLetter: InputState? = null,
        message: String? = null,
        ended: Ended? = null
    ) {
        val existing = viewState.value!!
        val bottom = if (message == null) {
            val rwCount = stats.allRightWrongCount()
            "Right: ${rwCount.first} Wrong: ${rwCount.second} " +
                    if (!state.started) "ENDED" else ""
        } else {
            message
        }
        val merged =
            ViewState(
                gridState = gridState ?: existing.gridState,
                missedPosition = missedPosition ?: existing.missedPosition,
                missedLetter = missedLetter ?: existing.missedLetter,
                trialsRemainingLabel = "${state.trialNumber}/${mode.num_trials_total}",
                nbackLevel = existing.nbackLevel,
                bottom = bottom,
                ended
            )
        Log.d("emmit", "emmit $merged")
        viewState.value = merged
    }


    private data class State(
        var started: Boolean = true,
        var tick: Int = 0,
        var trialNumber: Int = 0,
        var trial_starttime: Long = -1,
        var completed: Boolean = false
    )

    private var state = State()
    private val stats = MutableStats(mode.nback, mode.ticks_per_trial, mode.num_trials_total)

    private fun update() {
        state.tick++

        if (state.tick == 1) {
            Log.d("update", "tick==1 $state")
            state.trialNumber += 1
            state.trial_starttime = System.currentTimeMillis() / 1000
            if (state.trialNumber > mode.num_trials_total) {
                end_session(false)
            } else {
                generate_stimulus()
            }
            emmit(missedPosition = InputState.NEUTRAL, missedLetter = InputState.NEUTRAL)
        }

        if (state.tick == 6) { //        # Hide square at either the 0.5 second mark or sooner
            Log.d("update", "tick==6")
            emmit(gridState = GridState.Clear)
        }
        if (state.tick == mode.ticks_per_trial - 2) { //:  # display feedback for 200 ms
            Log.d("update", "tick==mode.ticks_per_trial - 2")

            // todo huh? state.tick = 0
            saveNoInput()
        }
        if (state.tick == mode.ticks_per_trial) {
            Log.d("update", "tick==mode.ticks_per_trial")
            state.tick = 0
        }
    }

    private fun checkMissed() {
        val positionMatch = thereIsAMatch(stats.stimuliPosition)
        val letterMatch = thereIsAMatch(stats.stimuliLetters)

        if (stats.positionClicked.size == state.trialNumber) {
            emmit(
                missedPosition = if (stats.positionClicked.last()) {
                    if (positionMatch) InputState.RIGHT else InputState.WRONG
                } else {
                    if (positionMatch) InputState.WRONG else InputState.NEUTRAL
                }
            )
        }
        if (stats.letterClicked.size == state.trialNumber) {
            emmit(
                missedLetter = if (stats.letterClicked.last()) {
                    if (letterMatch) InputState.RIGHT else InputState.WRONG
                } else {
                    if (letterMatch) InputState.WRONG else InputState.NEUTRAL
                }
            )
        }
    }

    private fun <T> thereIsAMatch(stimuli: List<T>): Boolean {
        val last = stimuli.last()
        val nback = if (stimuli.lastIndex - mode.nback < 0) {
            null
        } else {
            stimuli[stimuli.lastIndex - mode.nback]
        }
        return if (nback == null) false else last == nback
    }

    private fun saveNoInput() {
        if (stats.positionClicked.size == state.trialNumber - 1) {
            stats.positionClicked.add(false)
        }
        if (stats.letterClicked.size == state.trialNumber - 1) {
            stats.letterClicked.add(false)
        }

        checkMissed()
    }

    fun positionClicked() {
        if (stats.positionClicked.size == state.trialNumber - 1) {
            stats.positionClicked.add(true)
        }
        checkMissed()
    }

    fun letterClicked() {
        if (stats.letterClicked.size == state.trialNumber - 1) {
            stats.letterClicked.add(true)
        }
        checkMissed()
    }

    private fun generate_stimulus() {
        var position = Random.nextInt(0..7)
        var letter = Random.nextInt(1..9).toString()

        val backL: Int? = interference(stats.stimuliLetters)
        if (backL != null) {
            val nback_trial = state.trialNumber - backL - 1
            Log.d(
                "generate_stimulus",
                "pyIdx nback_trial $nback_trial = state.trialNumber ${state.trialNumber} - backL $backL - 1"
            )
            val matching_stim = stats.stimuliLetters.pyIdx(nback_trial)
            // check for collisions in multi-stim mode
            Log.d("generate_stimulus", "setting letters to $matching_stim")
            letter = matching_stim
        }

        val backP: Int? = interference(stats.stimuliPosition)
        if (backP != null) {
            val nback_trial = state.trialNumber - backP - 1
            val matching_stim = stats.stimuliPosition.pyIdx(nback_trial)
            Log.d("generate_stimulus", "setting position to $matching_stim")
            position = matching_stim
        }

        stats.stimuliLetters.add(letter)
        stats.stimuliPosition.add(position)

        emmit(gridState = GridState.Show(Entry(position, letter.toString())))
    }

    private fun <T> interference(stimuli: List<T>): Int? {
        var back: Int? = null

        if (state.trialNumber > mode.nback) {

            val real_back = mode.nback

            val r1 = Random.nextFloat();
            val r2 = Random.nextFloat()
            Log.d("interference", "r1=$r1 r2=$r2")

            if (r1 < mode.CHANCE_OF_GUARANTEED_MATCH
                || (r2 < mode.DEFAULT_CHANCE_OF_INTERFERENCE && mode.nback > 1)
            ) {
                back = real_back
                Log.d("interference", "back = $back")
            }

            if ((r1 > mode.CHANCE_OF_GUARANTEED_MATCH && r2 < mode.DEFAULT_CHANCE_OF_INTERFERENCE)
                && mode.nback > 1
            ) {
                var interference = listOf(-1, 1, real_back)
                if (back != null && back < 3) interference = listOf(1, real_back)
                interference = interference.shuffled()
                Log.d("interference", "interference=$interference")
                for (i in interference) {
                    if (state.trialNumber - (real_back + i) - 1 >= 0
                        && stimuli.pyIdx(state.trialNumber - (real_back + 1)) !=
                        stimuli.pyIdx(state.trialNumber - real_back - 1)
                    ) {
                        Log.d("interference", "back = real_back + 1: $back = $real_back + 1")
                        back = real_back + 1
                    }
                }
                if (back == real_back) back = null
                else Log.d("interference", "Forcing interference ")
            }
        }

        return back
    }

    /**
     * Python style index that wraps around for negative numbers. -1 is the last element
     */
    fun <T> List<T>.pyIdx(idx: Int): T {
        val newIdx = if (idx < 0) size + idx else idx
        Log.d("pyIdx", "if ($idx < 0) $size + $idx else $idx = $newIdx")
        Log.d("pyIdx", "$this")
        return get(newIdx)
    }

    fun start() {
        ticker.start()
        state.tick = -9
    }

    override fun onCleared() {
        if (!state.completed) end_session(true)
    }

    fun end_session(cancelled: Boolean) {
        Log.d("end_session", "")
        state.started = false
        ticker.stop()

        if (!cancelled) {
            statsRepo.write(stats.toStats())
            val newMode = modeRepo.endSession(mode, stats)

            state.completed = true

            val result = stats.result(mode)

            emmit(
                ended = Ended(
                    mode.nback,
                    result,
                    stats.totalScore,
                    stats.positionScore,
                    stats.letterScore,
                    newMode.progress,
                    newMode.nback,
                    mode.nback
                )
            )
        }
    }

}
