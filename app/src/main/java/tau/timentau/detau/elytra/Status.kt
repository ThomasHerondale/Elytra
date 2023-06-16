package tau.timentau.detau.elytra

typealias OperationStatus = Status<Unit>

sealed class Status<out T> {

    object Loading: Status<Nothing>()

    data class Success<T>(val data: T): Status<T>()

    data class Failed(val exception: Exception) : Status<Nothing>()

    companion object {
        fun <T> success(data: T) = Success(data)

        fun operationSuccess() = Success(Unit)

        fun failure(exception: Exception) = Failed(exception)

        fun loading() = Loading
    }
}