package miguel.brainz2

import android.app.Application
import miguel.brainz2.repos.ModeRepo
import miguel.brainz2.repos.StatsRepo

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        statsRepo = StatsRepo(this)
        modeRepo = ModeRepo(this)

    }
}

lateinit var statsRepo: StatsRepo
lateinit var modeRepo: ModeRepo