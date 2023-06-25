package tau.timentau.detau.elytra.accomodations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tau.timentau.detau.elytra.databinding.FragmentSearchAccomodationsBinding

class SearchAccomodationsFragment : Fragment() {

    private lateinit var binding: FragmentSearchAccomodationsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchAccomodationsBinding.inflate(inflater)

        return binding.root
    }
}