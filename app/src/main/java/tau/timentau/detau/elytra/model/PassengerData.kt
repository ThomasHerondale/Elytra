package tau.timentau.detau.elytra.model

const val HAND_LUGGAGE_PRICE = 17.90
const val CARGO_LUGGAGE_PRICE = 39.90

class PassengerData(
    val index: Int,
    flight: Flight,
    val forReturn: Boolean = false,
) {

    var name: String = "Passeggero $index"

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


    override fun toString(): String {
        return "PassengerData(index=$index, forReturn=$forReturn, name='$name', handLuggage=$handLuggage, cargoLuggage=$cargoLuggage, price=$price)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassengerData

        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }


}
