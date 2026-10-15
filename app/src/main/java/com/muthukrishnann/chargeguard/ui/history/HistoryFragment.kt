package com.muthukrishnann.chargeguard.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.muthukrishnann.chargeguard.data.AppDatabase
import com.muthukrishnann.chargeguard.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = AppDatabase.getDatabase(requireContext())

        adapter = HistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        binding.btnClearHistory.setOnClickListener {
            showClearConfirmationDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            database.chargingDao().getAllSessions().collectLatest { list ->
                adapter.submitList(list)
                if (list.isEmpty()) {
                    binding.rvHistory.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.btnClearHistory.visibility = View.GONE
                } else {
                    binding.rvHistory.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.btnClearHistory.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showClearConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear History")
            .setMessage("Do you want to permanently delete all recorded charging history?")
            .setPositiveButton("Clear") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    database.chargingDao().clearHistory()
                    Toast.makeText(requireContext(), "Charging history cleared", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
