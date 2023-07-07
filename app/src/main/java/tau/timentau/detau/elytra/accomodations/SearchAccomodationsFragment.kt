package tau.timentau.detau.elytra.accomodations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.FragmentSearchAccomodationsBinding
import tau.timentau.detau.elytra.fromMilliToReadable
import tau.timentau.detau.elytra.model.AccomodationCategory
import tau.timentau.detau.elytra.model.AccomodationCategory.APARTMENT
import tau.timentau.detau.elytra.model.AccomodationCategory.CAMPING
import tau.timentau.detau.elytra.model.AccomodationCategory.HOSTEL
import tau.timentau.detau.elytra.model.AccomodationCategory.HOTEL
import tau.timentau.detau.elytra.showNetworkErrorDialog
import tau.timentau.detau.elytra.text
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

private const val TAG = "SEARCH_ACCOMODATIONS"

class SearchAccomodationsFragment : Fragment() {

    private lateinit var binding: FragmentSearchAccomodationsBinding
    private val accomodationsViewModel: AccomodationsViewModel by activityViewModels()

    private inline val citiesField: MaterialAutoCompleteTextView
        get() = binding.cityText.editText as MaterialAutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchAccomodationsBinding.inflate(inflater)

        setupCityField()
        setupRatingSection()

        // imposta lo slider in euro senza centesimi
        binding.priceSlider.setLabelFormatter {
            val formatter = NumberFormat.getCurrencyInstance()
            formatter.minimumFractionDigits = 0
            formatter.currency = Currency.getInstance(Locale.ITALY)
            formatter.format(it)
        }

        binding.dateRangeText.editText?.setOnClickListener { showDateRangePickerDialog() }
        binding.dateRangeText.setEndIconOnClickListener { showDateRangePickerDialog() }

        binding.searchAccomodationsBttn.setOnClickListener {
            if (validateFields()) {
                performSearch()
            }
        }

        return binding.root
    }

    private fun performSearch() {
        val dialog = showProgressDialog()

        accomodationsViewModel.accomodationFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> {
                    Log.e(TAG, it.exception.stackTraceToString())
                    showNetworkErrorDialog()
                }
                is Status.Loading -> { }
                is Status.Success -> {
                    dialog.cancel()
                    Log.i("TAG", it.data.toString())
                }
            }
        }

        val selectedCategories = mutableListOf<AccomodationCategory>()
        binding.categoryChips.checkedChipIds.forEach {
            when (it) {
                R.id.hotelChip -> selectedCategories.add(HOTEL)
                R.id.hostelChip -> selectedCategories.add(HOSTEL)
                R.id.apartmentChip -> selectedCategories.add(APARTMENT)
                R.id.campingChip -> selectedCategories.add(CAMPING)
            }
        }

        accomodationsViewModel.getAccomodations(
            binding.cityText.text,
            binding.priceSlider.values[0].toDouble(),
            binding.priceSlider.values[1].toDouble(),
            binding.ratingBar.rating.toInt(),
            selectedCategories
        )
    }

    private fun validateFields() =
        validateCityField() and
        validateHostCountField() and
        validateDateRangeField()

    private fun validateDateRangeField(): Boolean {
        if (binding.dateRangeText.text.isBlank())
            binding.dateRangeText.error = getString(R.string.seleziona_periodo)
        else
            binding.dateRangeText.error = null

        return binding.dateRangeText.error == null
    }

    private fun validateCityField(): Boolean {
        if (binding.cityText.text.isBlank()) {
            binding.cityText.error = getString(R.string.inserisci_citta_valida)
        } else {
            val cityNames = accomodationsViewModel.cities.map { it.name }
            if (!cityNames.contains(binding.cityText.text))
                binding.cityText.error = getString(R.string.inserisci_citta_valida)
            else
                binding.cityText.error = null
        }

        return binding.cityText.error == null
    }

    private fun validateHostCountField(): Boolean {
        if (binding.hostCountText.text.isBlank() || binding.hostCountText.text.toInt() <= 0)
            binding.hostCountText.error = " "
        else
            binding.hostCountText.error = null

        return binding.hostCountText.error == null
    }

    private fun setupRatingSection() {
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            val ratingStr = when (val stars = rating.toInt()) {
                0 -> ""
                1 -> getString(R.string.una_stella_o_piu)
                2, 3, 4 -> getString(R.string.stelle_o_piu, stars)
                5 -> getString(R.string.cinque_stelle)
                else -> throw IllegalArgumentException("Invalid rating")
            }
            binding.ratingText.text = ratingStr
        }
    }

    private fun setupCityField() {
        accomodationsViewModel.citiesFetchStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Status.Failed -> showNetworkErrorDialog()
                is Status.Loading -> { showOrHideProgressBar(false) }
                is Status.Success -> {
                    val items = status.data.map { it.toString() }.toTypedArray()
                    citiesField.setSimpleItems(items)

                    showOrHideProgressBar(true)
                }
            }
        }

        accomodationsViewModel.loadCities()

        citiesField.dropDownHeight = 1000
    }

    private fun showDateRangePickerDialog() {
        val constraints = CalendarConstraints.Builder()
            .setFirstDayOfWeek(Calendar.MONDAY)
            .setValidator(DateValidatorPointForward.now())
            .build()

        val dialog = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Seleziona il periodo")
            .setCalendarConstraints(constraints)
            .build()

        dialog.addOnPositiveButtonClickListener {
            val startDate = fromMilliToReadable(it.first)
            val endDate = fromMilliToReadable(it.second)
            binding.dateRangeText.editText?.setText(
                getString(
                    R.string.range_date_str,
                    startDate,
                    endDate
                )
            )
        }

        dialog.show(parentFragmentManager, "pickRange")

    }

    private fun showProgressDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(
            requireContext(),
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setView(R.layout.dialog_progress_simple)
            .setCancelable(false)
            .show()
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.cityFetchProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }
}