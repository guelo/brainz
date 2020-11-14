package miguel.brainz2.ui.mode

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import miguel.brainz2.databinding.ModeFragmentBinding
import miguel.brainz2.databinding.ModeRowBinding
import miguel.brainz2.repos.Mode

class ModeFragment : Fragment() {

    private val viewModel by viewModels<ModeFragmentViewModel>()

    private var _binding: ModeFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ModeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ModeAdapter()
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext())

        viewModel.modeLD.observe(viewLifecycleOwner) { mode ->
            adapter.set(mode)
        }
    }

    inner class ModeAdapter : RecyclerView.Adapter<VH>() {

        private var mode: Mode = Mode(0)

        private fun list() = listOf<Pair<String, Any>>(
            "nback" to mode.nback,
            "session_number" to mode.session_number,
            "progress" to mode.progress,
            "num_trials" to mode.num_trials,
            "num_trials_factor" to mode.num_trials_factor,
            "num_trials_exponent" to mode.num_trials_exponent,
            "ticks_per_trial" to mode.ticks_per_trial,
            "CHANCE_OF_GUARANTEED_MATCH" to mode.CHANCE_OF_GUARANTEED_MATCH,
            "DEFAULT_CHANCE_OF_INTERFERENCE" to mode.DEFAULT_CHANCE_OF_INTERFERENCE,
            "THRESHOLD_ADVANCE" to mode.THRESHOLD_ADVANCE,
            "THRESHOLD_FALLBACK" to mode.THRESHOLD_FALLBACK,
            "THRESHOLD_FALLBACK_SESSIONS" to mode.THRESHOLD_FALLBACK_SESSIONS,
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ModeRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(list()[position])
        }

        override fun getItemCount() = list().size

        fun set(mode: Mode) {
            this.mode = mode
            notifyDataSetChanged()
        }
    }

    inner class VH(private val binding: ModeRowBinding) : RecyclerView.ViewHolder(binding.root) {
        private val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                viewModel.change(key, s.toString())
            }
        }
        private var key = ""
        fun bind(row: Pair<String, Any>) {
            binding.label.text = row.first
            key = row.first
            binding.value.removeTextChangedListener(textWatcher)
            binding.value.setText(row.second.toString())
            binding.value.addTextChangedListener(textWatcher)
        }
    }

}