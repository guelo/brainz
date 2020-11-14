package miguel.brainz2.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import miguel.brainz2.repos.Mode
import miguel.brainz2.repos.Stats
import miguel.brainz2.modeRepo
import miguel.brainz2.statsRepo

class MainViewModel : ViewModel() {

    val liveData = MutableLiveData<Pair<Mode, List<Stats>>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            modeRepo.listen().flatMapLatest {
                statsRepo
                    .listen()
                    .map { stat -> Pair(it, stat) }
            }
                .collect {
                    withContext(Dispatchers.Main) {
                        liveData.value = it
                    }

                }
        }

    }
}