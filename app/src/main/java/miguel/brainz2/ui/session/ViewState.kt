package miguel.brainz2.ui.session

import miguel.brainz2.grid.Entry

sealed class GridState {
    data class Show(val entry: Entry) : GridState()
    object Clear : GridState()
}

enum class InputState {
    NEUTRAL, WRONG, RIGHT
}

data class Ended(
    val nback: Int,
    val result: Result,
    val total: Int,
    val position: Int,
    val shape: Int,
    val strikes: Int,
    val newLevel: Int,
    val oldLevel: Int,

    ) {
    enum class Result {LOW, NEUTRAL, UP}
}

data class ViewState(
    val gridState: GridState,
    val missedPosition: InputState,
    val missedLetter: InputState,
    val trialsRemainingLabel: String,
    val nbackLevel: String,
    val bottom: String,
    val ended: Ended?
)
