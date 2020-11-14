package miguel.brainz2.ui.session

import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow

class Ticker(periodMillis: Long) {
    private val tickerChannel = ticker(delayMillis = periodMillis, initialDelayMillis = 0).receiveAsFlow()

    private var paused = true

    fun tick() = tickerChannel.filter { !paused }

    fun start() {
        paused = false
    }

    fun stop() {
        paused = true
    }
}