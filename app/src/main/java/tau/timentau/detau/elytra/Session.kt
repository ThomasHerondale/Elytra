package tau.timentau.detau.elytra

object Session {
    private var _loggedEmail: String? = null
    // vista non nullabile
    val loggedEmail: String
        get() {
            return _loggedEmail ?:
                throw IllegalStateException("Could not retrieve logged email from current session")
        }

    fun login(email: String) {
        if (_loggedEmail != null)
            throw IllegalStateException("Can't overwrite existing session, invalidate it first")
        else
            _loggedEmail = email
    }

    fun invalidate() {
        if (_loggedEmail == null)
            throw IllegalStateException("Session is already invalid")
        else
            _loggedEmail = null
    }
}