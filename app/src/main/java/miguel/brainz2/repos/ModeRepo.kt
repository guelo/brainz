package miguel.brainz2.repos

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.lang.IllegalArgumentException

class ModeRepo(application: Context) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private var jsonAdapter = moshi.adapter(Mode::class.java)

    private val file = File(application.filesDir, "mode")

    init {
        if (!file.exists()) {
            val initialMode = Mode(2, 0, 0)
            file.writeText(jsonAdapter.toJson(initialMode))
        }
    }

    fun read(): Mode {
        val file = file.readText()
        return jsonAdapter.fromJson(file)!!
    }

    fun write(mode: Mode) {
        validate(mode)
        val json = jsonAdapter.toJson(mode)
        file.writeText(json)
        flow.value = mode
    }

    private fun validate(mode: Mode) {
        if (
            mode.ticks_per_trial < 20
            || mode.CHANCE_OF_GUARANTEED_MATCH > 1
            || mode.DEFAULT_CHANCE_OF_INTERFERENCE > 1
            || mode.THRESHOLD_ADVANCE <50
            || mode.THRESHOLD_FALLBACK < 20
        ) {
            throw IllegalArgumentException("bad mode $mode")
        }
    }

    fun endSession(mode: Mode, stats: MutableStats): Mode {
        var strikes = mode.progress
        var nback = mode.nback

        if (stats.totalScore > mode.THRESHOLD_ADVANCE) {
            nback += 1
            strikes = 0
            println("endSession 1")
        } else if (stats.totalScore < mode.THRESHOLD_FALLBACK) {
            if (strikes == mode.THRESHOLD_FALLBACK_SESSIONS) {
                nback -= 1
                strikes = 0
                println("endSession 2")
            } else {
                strikes += 1
                println("endSession 3")
            }
        } else {
            println("endSession 4")
        }


        val newMode = mode.copy(
            nback = nback,
            progress = strikes,
            session_number = mode.session_number + 1
        )

        write(newMode)

        println("else ${stats.totalScore} < ${mode.THRESHOLD_FALLBACK} ${mode.THRESHOLD_ADVANCE}")
        println("endSession $newMode")

        return newMode
    }

    private val flow = MutableStateFlow(read())

    fun listen() = flow.asStateFlow()

}