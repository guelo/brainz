package miguel.brainz2

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        for (i in 0..9) {
            println("$i % 3 = ${i%3}  $i / 3 = ${i/3}")
        }
    }
}