package miguel.brainz2.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import miguel.brainz2.R
import miguel.brainz2.databinding.MainFragmentBinding
import miguel.brainz2.databinding.StatsRowBinding
import miguel.brainz2.repos.Mode
import miguel.brainz2.repos.Stats
import miguel.brainz2.repos.result
import miguel.brainz2.ui.mode.ModeFragment
import miguel.brainz2.ui.session.Ended
import miguel.brainz2.ui.session.SessionFragment
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class MainFragment() : Fragment() {

    private val viewModel by viewModels<MainViewModel>()

    private var _binding: MainFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnStart.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, SessionFragment.newInstance(), "SessionFragment")
                .addToBackStack(null)
                .commit();
        }

        binding.btnSettings.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, ModeFragment(), "ModeFragment")
                .addToBackStack(null)
                .commit();
        }

        binding.stathistory.layoutManager = LinearLayoutManager(requireContext())
        val adapter = HistoryAdapter()
        binding.stathistory.adapter = adapter

        viewModel.liveData.observe(viewLifecycleOwner) { (mode, stats) ->
            adapter.set(stats, mode)
            populateChart(stats)

            binding.tvMode.text =
                "Dual ${mode.nback}-back.... ${mode.progress}/${mode.THRESHOLD_FALLBACK_SESSIONS} strikes"

        }

    }

    private fun populateChart(it: List<Stats>) {
        val entries = it.mapIndexed { idx, stat ->
            Entry(idx.toFloat(), (stat.nback + stat.totalScore / 100.0).toFloat())
        }

        val dataSet = LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(R.color.design_default_color_primary_dark)
        dataSet.setValueTextColor(R.color.teal_200)

        val lineData = LineData(dataSet)
        binding.chart.setData(lineData)
        binding.chart.invalidate()
    }

    inner class HistoryAdapter : RecyclerView.Adapter<VH>() {

        private var lst: List<Stats> = mutableListOf()
        var mode = Mode(0)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = StatsRowBinding.inflate(LayoutInflater.from(parent.getContext()))
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(lst[position], mode)
        }

        override fun getItemCount() = lst.size

        fun set(list: List<Stats>, mode: Mode) {
            lst = list.sortedByDescending { it.startTime }
            notifyDataSetChanged()
        }
    }

    class VH(private val binding: StatsRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stat: Stats, mode: Mode) {

            val time = Instant.ofEpochMilli(stat.startTime)
                .atZone(ZoneId.of("America/Los_Angeles"))
                .format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
            binding.tv.text = "$time ${stat.mode} ${stat.totalScore} "



            when (stat.result(mode)) {
                Ended.Result.LOW -> binding.tv.setTextColor(Color.RED)
                Ended.Result.NEUTRAL -> binding.tv.setTextColor(Color.GRAY)
                Ended.Result.UP -> binding.tv.setTextColor(Color.GREEN)
            }
        }
    }
}