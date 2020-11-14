package miguel.brainz2.repos

import miguel.brainz2.ui.session.Ended
import java.lang.IllegalArgumentException

interface Stats {
    val nback: Int
    val ticks_per_trial: Int
    val num_trials_total: Long
    val stimuliLetters: List<String>
    val stimuliPosition: List<Int>
    val positionClicked: List<Boolean>
    val letterClicked: List<Boolean>
    val startTime: Long
    val mode: String
    val totalScore: Int
    val letterScore: Int
    val positionScore: Int
}

data class ImmutableStats(
    override val nback: Int,
    override val ticks_per_trial: Int,
    override val num_trials_total: Long,
    override val stimuliLetters: List<String>,
    override val stimuliPosition: List<Int>,
    override val positionClicked: List<Boolean>,
    override val letterClicked: List<Boolean>,
    override val startTime: Long,
    override val mode: String,
    override val totalScore: Int,
    override val letterScore: Int,
    override val positionScore: Int,
): Stats

class MutableStats(
    override val nback: Int,
    override val ticks_per_trial: Int,
    override val num_trials_total: Long
): Stats {
    override val stimuliLetters: MutableList<String> = mutableListOf()
    override val stimuliPosition: MutableList<Int> = mutableListOf()
    override val positionClicked: MutableList<Boolean> = mutableListOf()
    override val letterClicked: MutableList<Boolean> = mutableListOf()

    override val startTime = System.currentTimeMillis()
    override val mode = "D$nback" + "B"
    override val totalScore
        get() = totalPercent()
    override val letterScore
        get() = calcPercents(letterRwCount())
    override val positionScore
        get() = calcPercents(positionRwCount())

    private fun totalPercent(): Int {
        val rwPosition = rightWrongCount(stimuliPosition, positionClicked, nback)
        val rwLetters = rightWrongCount(stimuliLetters, letterClicked, nback)

        return calcPercents(
            Pair(
                rwPosition.first + rwLetters.first,
                rwPosition.second + rwLetters.second
            )
        )
    }

    private fun positionRwCount() = rightWrongCount(stimuliPosition, positionClicked, nback)
    private fun letterRwCount() = rightWrongCount(stimuliLetters, letterClicked, nback)

    private fun calcPercents(rw: Pair<Int, Int>): Int {
        return if (rw.first + rw.second == 0) 0
        else (rw.first * 100 / (rw.first + rw.second).toFloat()).toInt()
    }

    fun allRightWrongCount(): Pair<Int, Int> {
        val positionCount =
            rightWrongCount(stimuliPosition, positionClicked, nback)
        val letterCount = rightWrongCount(stimuliLetters, letterClicked, nback)
        return Pair(
            positionCount.first + letterCount.first,
            positionCount.second + letterCount.second
        )
    }

    private fun <T> rightWrongCount(
        stimuli: List<T>,
        clicked: List<Boolean>,
        nback: Int
    ): Pair<Int, Int> {
        var rights = 0
        var wrongs = 0
        rightWrongs(stimuli, clicked, nback).forEach {
            if (it != null) if (it) rights++ else wrongs++
        }
        return Pair(rights, wrongs)
    }

    /**
     * true = right, false = wrong
     */
    private fun <T> rightWrongs(
        stimuli: List<T>,
        clicked: List<Boolean>,
        nback: Int
    ): List<Boolean?> {
        val usedStimuli = when (stimuli.size) {
            clicked.size + 1 -> stimuli.subList(0, stimuli.size - 1)
            clicked.size -> stimuli
            else -> throw IllegalArgumentException()
        }

        return usedStimuli.mapIndexed { i, t ->
            val match = if (i <= nback) false
            else t == usedStimuli[i - nback]

            if (!match && !clicked[i]) null
            else clicked[i] == match
        }
    }

    fun toStats() = ImmutableStats(
        nback,
        ticks_per_trial,
        num_trials_total,
        stimuliLetters,
        stimuliPosition,
        positionClicked,
        letterClicked,
        startTime,
        mode,
        totalScore,
        letterScore,
        positionScore
    )
}

fun Stats.result(mode: Mode) = when {
    totalScore > mode.THRESHOLD_ADVANCE -> Ended.Result.UP
    totalScore < mode.THRESHOLD_FALLBACK -> Ended.Result.LOW
    else -> Ended.Result.NEUTRAL
}
