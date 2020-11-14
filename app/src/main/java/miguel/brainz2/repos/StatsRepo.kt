package miguel.brainz2.repos

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.lang.reflect.Type

class StatsRepo(application: Context) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    var type: Type = Types.newParameterizedType(MutableList::class.java, ImmutableStats::class.java)
    private var jsonAdapter = moshi.adapter<List<ImmutableStats>>(type)

    private val file = File(application.filesDir, "stats")

    init {
        if (!file.exists()) {
            file.writeText("[]")
        }
    }

    fun write(stats: ImmutableStats) {
        val prevHistory = read()
        val newList = prevHistory.plus(stats)
        val json = jsonAdapter.toJson(newList)
        file.writeText(json)
        flow.value = newList
    }

    fun read(): List<ImmutableStats> {
        val file = file.readText()
        return jsonAdapter.fromJson(file)!!
    }

    private val flow = MutableStateFlow(read())

    fun listen() = flow.asStateFlow()

}