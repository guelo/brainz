package miguel.brainz2.ui.session

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import miguel.brainz2.R
import miguel.brainz2.databinding.SessionFragmentBinding

class SessionFragment : Fragment() {

    companion object {
        fun newInstance() = SessionFragment()
    }

    private val viewModel by viewModels<SessionViewModel>()

    private var _binding: SessionFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SessionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.viewState.observe(viewLifecycleOwner) {
            updateGridState(it.gridState)
            updateMissed(it.missedLetter, it.missedPosition)
            binding.trialcount.text = it.trialsRemainingLabel
            binding.nbacklabel.text = it.nbackLevel
            binding.message.text = it.bottom

            if (it.ended != null) {
                end(it.ended)
            }
        }

        view.findViewById<Button>(R.id.letter).setOnClickListener {
            viewModel.letterClicked()
        }
        view.findViewById<Button>(R.id.position).setOnClickListener {
            viewModel.positionClicked()
        }

        viewModel.start()
    }

    private fun end(ended: Ended) {

        val message = """
Total ${ended.total}%

Position: ${ended.position}%
Letter: ${ended.shape}%

        """.plus(
            if (ended.newLevel > ended.oldLevel) {
                "Woohoo movin up to level ${ended.newLevel} !!!"
            } else if (ended.newLevel < ended.oldLevel) {
                "Dropped to level ${ended.newLevel}"
            } else {
                "Strikes: ${ended.strikes}"
            }
        )

        activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Dual ${ended.nback} back Result: ${ended.result}")
                .setMessage(message)
                .setNeutralButton("Close") { _, _ ->
                    parentFragmentManager.popBackStackImmediate()
                }
                .create()
                .show()
        }
    }

    private fun updateMissed(missedLetter: InputState, missedPosition: InputState) {

        fun inputColor(inputState: InputState) =
            when (inputState) {
                InputState.RIGHT -> Color.GREEN
                InputState.NEUTRAL -> Color.GRAY
                InputState.WRONG -> Color.RED
            }

        binding.letter.setBackgroundColor(inputColor(missedLetter))
        binding.position.setBackgroundColor(inputColor(missedPosition))
    }

    private fun updateGridState(gridState: GridState) {
        when (gridState) {
            is GridState.Show -> binding.grid.show(gridState.entry)
            GridState.Clear -> binding.grid.clearAll()
        }
    }

}