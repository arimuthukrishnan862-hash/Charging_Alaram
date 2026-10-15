package com.muthukrishnann.chargeguard.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.muthukrishnann.chargeguard.data.ChargingSession
import com.muthukrishnann.chargeguard.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<ChargingSession, HistoryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

        fun bind(session: ChargingSession) {
            val startFormatted = dateFormat.format(Date(session.startTime))
            binding.tvSessionDate.text = startFormatted

            val gain = session.endLevel - session.startLevel
            val gainSign = if (gain >= 0) "+$gain%" else "$gain%"
            binding.tvLevelChange.text = "${session.startLevel}% ➔ ${session.endLevel}% ($gainSign)"

            val mins = session.durationSeconds / 60
            val secs = session.durationSeconds % 60
            binding.tvDuration.text = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"

            binding.tvPeakLevel.text = "Peak: ${session.maxLevel}%"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ChargingSession>() {
        override fun areItemsTheSame(oldItem: ChargingSession, newItem: ChargingSession) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChargingSession, newItem: ChargingSession) = oldItem == newItem
    }
}
