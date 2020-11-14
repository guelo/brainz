package miguel.brainz2.grid

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import miguel.brainz2.R
import kotlin.math.min

class Grid @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    init {
        initializeViews(context);
    }

    private lateinit var tv11: TextView
    private lateinit var tv12: TextView
    private lateinit var tv13: TextView
    private lateinit var tv21: TextView
    private lateinit var tv22: TextView
    private lateinit var tv23: TextView
    private lateinit var tv31: TextView
    private lateinit var tv32: TextView
    private lateinit var tv33: TextView

    private val allTv =
        listOf(listOf(tv11, tv12, tv13), listOf(tv21, tv22, tv23), listOf(tv31, tv32, tv33))

    private fun initializeViews(context: Context) {
        inflate(context, R.layout.grid_layout, this)

        tv11 = findViewById(R.id.a11)
        tv12 = findViewById(R.id.a12)
        tv13 = findViewById(R.id.a13)
        tv21 = findViewById(R.id.a21)
        tv22 = findViewById(R.id.a22)
        tv22.text = "+"
        tv23 = findViewById(R.id.a23)
        tv31 = findViewById(R.id.a31)
        tv32 = findViewById(R.id.a32)
        tv33 = findViewById(R.id.a33)
    }

    fun show(entry: Entry) {
        clearAll()
        findTv(entry.x).text = entry.str
    }

    private fun findTv(x: Int): TextView {
        val realx = if (x > 3) x + 1 else x
        println("findTv $realx $x")
        return allTv[realx / 3][realx % 3]
    }

    fun clearAll() {
        allTv.forEach {
            it.forEach {
                if (it != tv22) it.text = ""
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val min = min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(min, min);
    }
}