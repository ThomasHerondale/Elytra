package tau.timentau.detau.elytra

import androidx.navigation.NavDirections

interface NavHostActivity {
    fun navigateTo(directions: NavDirections)

    fun popBackStack() {
        throw UnsupportedOperationException()
    }
}
