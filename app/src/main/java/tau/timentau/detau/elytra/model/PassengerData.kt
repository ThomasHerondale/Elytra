package tau.timentau.detau.elytra.model

data class PassengerData(
    var name: String,
    var handLuggage: Boolean = false,
    var cargoLuggage: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassengerData

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
