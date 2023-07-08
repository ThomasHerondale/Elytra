package tau.timentau.detau.elytra.discover

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.FragmentDiscoverBinding
import tau.timentau.detau.elytra.showNetworkErrorDialog

private const val TAG = "DISCOVER"

class DiscoverFragment : Fragment() {

    private lateinit var binding: FragmentDiscoverBinding
    private val viewModel: DiscoverViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDiscoverBinding.inflate(inflater)

        // se non c'è stato salvato -> se il fragment sta venendo creato per la prima volta
        if (savedInstanceState == null) {
            setupMostFamousDestinationsSection()

            setupFutureDestinationsSection()
        }

        return binding.root
    }

    private fun setupFutureDestinationsSection() {
        viewModel.futureDestinationsFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> {
                    Log.e(TAG, it.exception.stackTraceToString())
                    showNetworkErrorDialog()
                }

                is Status.Loading -> {
                    showOrHideProgressBar(binding.futureProgress, false)
                }

                is Status.Success -> {
                    showOrHideProgressBar(binding.futureProgress, true)
                    if (it.data.isNotEmpty()) {
                        parentFragmentManager.beginTransaction()
                            .add(R.id.futureCitiesFragContainer, FutureDestinationsFragment())
                            .commit()
                    }
                }
            }
        }

        viewModel.getFutureDestinations()
    }

    private fun setupMostFamousDestinationsSection() {
        viewModel.mostFamousDestinationsFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> {
                    Log.e(TAG, it.exception.stackTraceToString())
                    showNetworkErrorDialog()
                }

                is Status.Loading -> {
                    showOrHideProgressBar(binding.mostFamousProgress, false)
                }

                is Status.Success -> {
                    showOrHideProgressBar(binding.mostFamousProgress, true)
                    // se c'è almeno una destinazione da mostrare, inserisci il fragment
                    if (it.data.isNotEmpty()) {
                        parentFragmentManager.beginTransaction()
                            .add(R.id.mostFamousFragContainer, MostFamousDestinationsFragment())
                            .commit()
                    }
                }
            }
        }

        viewModel.getMostFamousDestinations()
    }

    private fun showOrHideProgressBar(progressBar: ProgressBar, hide: Boolean) {
        progressBar.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

}