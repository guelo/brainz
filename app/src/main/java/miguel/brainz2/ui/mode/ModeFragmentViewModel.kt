package miguel.brainz2.ui.mode

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import miguel.brainz2.repos.Mode
import miguel.brainz2.modeRepo

class ModeFragmentViewModel : ViewModel() {

    private val changes = mutableMapOf<String, String>()
    fun change(key: String, value: String) {
        changes[key] = value
    }

    val modeLD = MutableLiveData<Mode>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            modeRepo
                .listen()
                .collect { mode ->
                    withContext(Dispatchers.Main) {
                        modeLD.value = mode
                    }
                }
        }
    }

    override fun onCleared() {
        val mode = modeLD.value!!

        if (changes.isNotEmpty()) {
            modeRepo.write(
                Mode(
                    nback = changes["nback"]?.toInt() ?: mode.nback,
                    session_number = changes["session_number"]?.toInt() ?: mode.session_number,
                    progress = changes["progress"]?.toInt() ?: mode.progress,
                    num_trials = changes["num_trials"]?.toInt() ?: mode.num_trials,
                    num_trials_factor = changes["num_trials_factor"]?.toInt()
                        ?: mode.num_trials_factor,
                    num_trials_exponent = changes["num_trials_exponent"]?.toInt()
                        ?: mode.num_trials_exponent,
                    ticks_per_trial = changes["ticks_per_trial"]?.toInt() ?: mode.ticks_per_trial,
                    CHANCE_OF_GUARANTEED_MATCH = changes["CHANCE_OF_GUARANTEED_MATCH"]?.toDouble()
                        ?: mode.CHANCE_OF_GUARANTEED_MATCH,
                    DEFAULT_CHANCE_OF_INTERFERENCE = changes["DEFAULT_CHANCE_OF_INTERFERENCE"]?.toDouble()
                        ?: mode.DEFAULT_CHANCE_OF_INTERFERENCE,
                    THRESHOLD_ADVANCE = changes["THRESHOLD_ADVANCE"]?.toInt()
                        ?: mode.THRESHOLD_ADVANCE,
                    THRESHOLD_FALLBACK = changes["THRESHOLD_FALLBACK"]?.toInt()
                        ?: mode.THRESHOLD_FALLBACK,
                    THRESHOLD_FALLBACK_SESSIONS = changes["THRESHOLD_FALLBACK_SESSIONS"]?.toInt()
                        ?: mode.THRESHOLD_FALLBACK_SESSIONS,

                    )
            )
        }

    }
}
