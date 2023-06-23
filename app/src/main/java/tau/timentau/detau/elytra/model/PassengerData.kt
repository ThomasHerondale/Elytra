package tau.timentau.detau.elytra.model

const val HAND_LUGGAGE_PRICE = 17.90
const val CARGO_LUGGAGE_PRICE = 39.90

class PassengerData(
    index: Int,
    flight: Flight,
    val forReturn: Boolean = false,
) {

    val name: String = "Passegero $index"

    var handLuggage: Boolean = false
        set(value) {
            if (value)
                price += HAND_LUGGAGE_PRICE
            else
                price -= HAND_LUGGAGE_PRICE

            field = value
        }

    var cargoLuggage: Boolean = false
        set(value) {
            if (value)
                price += CARGO_LUGGAGE_PRICE
            else
                price -= CARGO_LUGGAGE_PRICE

            field = value
        }

    var price: Double = flight.price

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassengerData

        if (name != other.name) return false
        return forReturn == other.forReturn
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + forReturn.hashCode()
        return result
    }

    override fun toString(): String {
        return "PassengerData(forReturn=$forReturn, name='$name', handLuggage=$handLuggage, cargoLuggage=$cargoLuggage, price=$price)"
    }


}
