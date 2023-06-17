package tau.timentau.detau.elytra.model

data class Airport(
    val code: String,
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Airport

        return code == other.code
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }
}
