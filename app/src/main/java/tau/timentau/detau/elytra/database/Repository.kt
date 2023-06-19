package tau.timentau.detau.elytra.database

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import tau.timentau.detau.elytra.model.Airport
import tau.timentau.detau.elytra.model.Company
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.model.Sex
import tau.timentau.detau.elytra.model.User
import tau.timentau.detau.elytra.toDateString
import java.util.Date

object Repository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun userExists(email: String, password: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<Boolean>("""
                SELECT EXISTS(
                    SELECT *
                    FROM users
                    WHERE users.email = '$email' AND users.password = '$password'
                ) as boolean
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun fetchUserData(email: String): Deferred<User> {
        return coroutineScope.async {
            val userDTO = DatabaseDAO.selectValue<UserDTO>("""
                SELECT email, fullName, birthDate, sex, avatar_images.path as avatar, 
                    password, security_questions.question as question, answer
                FROM users JOIN avatar_images on users.avatar = avatar_images.id
                    JOIN security_questions on users.question = security_questions.id
                WHERE email = '$email';
            """) ?: throw IllegalStateException("Could not retrieve data")

            userDTO.toUser()
        }
    }

    suspend fun isMailUsed(email: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<Boolean>("""
                SELECT EXISTS(
                    SELECT *
                    FROM users
                    WHERE users.email= '$email'
                ) as boolean
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun createUser(
        email: String,
        fullName: String,
        birthDate: LocalDate,
        sex: Sex,
        password: String
    ) {
        DatabaseDAO.insert("""
            INSERT
            INTO users(email, fullName, birthDate, sex, password)
            VALUE ('$email', '$fullName', '${birthDate.toDateString()}', '$sex', '$password')
        """)
    }

    suspend fun getSecurityQuestion(email: String): Deferred<String> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<String>("""
                SELECT security_questions.question as string
                FROM users JOIN security_questions on users.question = security_questions.id
                WHERE users.email = '$email' 
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun isAnswerCorrect(email: String, answer: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<Boolean>("""
                SELECT EXISTS(
                    SELECT *
                    FROM users
                    WHERE users.email = '$email' and answer = '$answer'
                ) as boolean
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun resetPassword(email: String, newPassword: String) {
        DatabaseDAO.update("""
            UPDATE users
            SET password = '$newPassword'
            WHERE email = '$email' 
        """)
    }

    suspend fun getSecurityQuestions(): Deferred<List<String>> {
        return coroutineScope.async {
            DatabaseDAO.selectList<QuestionDTO>("""
                SELECT question
                FROM security_questions
            """).map { it.question }
        }
    }

    suspend fun isFirstAccess(email: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<Boolean>("""
                SELECT (question IS NULL) as boolean
                FROM users
                WHERE email = '$email'
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun setupSecurityQuestion(email: String, question: String, answer: String) {
        DatabaseDAO.update("""
            UPDATE users u
            SET u.question = (
                SELECT sq.id
                FROM security_questions sq
                WHERE sq.question = '$question'
                ),
                u.answer = '$answer'
            WHERE u.email = '$email'
        """)
    }

    suspend fun getAvatars(): Deferred<List<Bitmap>> {
        return coroutineScope.async {
            val paths = DatabaseDAO.selectList<AvatarDTO>("""
                SELECT *
                FROM avatar_images
            """).map { it.path }

            val images = mutableListOf<Bitmap>()

            for (path in paths) {
                images.add(
                    DatabaseDAO.getImage(path)
                )
            }

            images
        }
    }

    suspend fun setAvatar(email: String, id: Int) {
        val dbId = id + 1 // gli indici generati partono da 1
        DatabaseDAO.update("""
            UPDATE users
            SET avatar = $dbId
            WHERE email = '$email'
        """)
    }

    suspend fun getAirports(): Deferred<List<Airport>> {
        return coroutineScope.async {
            DatabaseDAO.selectList<Airport>("""
                SELECT *
                FROM airports
            """)
        }
    }

    suspend fun getCompanies(): Deferred<List<Company>> {
        return coroutineScope.async {
            val companyDtos = DatabaseDAO.selectList<CompanyDTO>("""
                SELECT name, logo
                FROM companies
            """)

            println(companyDtos)
            val paths = companyDtos.map { it.logo }
            println(paths)
            val logos = mutableMapOf<String, Bitmap>()

            paths.forEach { logos[it] = DatabaseDAO.getImage(it) }
            companyDtos.map { it.toCompany(logos[it.logo]!!) }
        }
    }

    suspend fun getFlights(
        departureApt: String,
        arrivalApt: String,
        date: LocalDate,
        priceRange: Pair<Double, Double>,
        passengersCount: Int,
        economy: Boolean = false,
        business: Boolean = false,
        firstClass: Boolean = false): Deferred<List<Flight>> {

         return coroutineScope.async {
             if (!(economy || business || firstClass))
                throw IllegalArgumentException("Cannot fetch flights: no service class provided")

             // ottieni prima le informazioni sulle compagnie
             val companies = getCompanies().await()

                // ottieni le informazioni sugli aeroporti
                val airports = getAirports().await()

                val (minPrice, maxPrice) = priceRange

                val flightsDTOs = mutableListOf<FlightDTO>()

                // aggiungi i voli richiesti
                if (economy) {
                    flightsDTOs.addAll(
                        getFlightsForServiceClass(
                            departureApt,
                            arrivalApt,
                            date,
                            minPrice,
                            maxPrice,
                            passengersCount,
                            "economy"
                        )
                    )
                }

                if (business) {
                    flightsDTOs.addAll(
                        getFlightsForServiceClass(
                            departureApt,
                            arrivalApt,
                            date,
                            minPrice,
                            maxPrice,
                            passengersCount,
                            "business"
                        )
                    )
                }

                if (firstClass) {
                    flightsDTOs.addAll(
                        getFlightsForServiceClass(
                            departureApt,
                            arrivalApt,
                            date,
                            minPrice,
                            maxPrice,
                            passengersCount,
                            "firstClass"
                        )
                    )
                }

            flightsDTOs.map { it.toFlight(companies, airports) }
        }
    }

    private suspend fun getFlightsForServiceClass(
        departureApt: String,
        arrivalApt: String,
        date: LocalDate,
        minPrice: Double,
        maxPrice: Double,
        passengersCount: Int,
        serviceClass: String
    ): List<FlightDTO> {
        return DatabaseDAO.selectList<FlightDTO>("""
            SELECT f.id, f.company, f.departureApt, f.arrivalApt, f.date, 
                f.gateClosingTime, f.departureTime, f.arrivalTime, f.duration, 
                fp.${serviceClass}Price as price
            FROM flights f JOIN
                flights_free_seats ffs ON f.id = ffs.id JOIN
                flights_prices fp ON f.id = fp.id
            WHERE f.departureApt = '$departureApt' AND
                f.arrivalApt = '$arrivalApt' AND
                f.date = '${date.toDateString()}' AND
                fp.${serviceClass}Price BETWEEN $minPrice AND $maxPrice AND
                ffs.${serviceClass}Free >= $passengersCount
            """)
    }

    private class UserDTO(
        val email: String,
        val fullName: String,
        val birthDate: Date,
        val sex: Sex,
        val avatar: String,
        val password: String,
        val question: String,
        val answer: String
    ) {
        fun toUser() = User(email, fullName, birthDate, sex)
    }

    private class QuestionDTO(
        val question: String
    )

    private class AvatarDTO(
        val id: Int,
        val path: String
    )

    private class CompanyDTO(
        val name: String,
        val logo: String
    ) {
        fun toCompany(logo: Bitmap) = Company(name, logo)
    }

    data class FlightDTO(
        val id: String,
        val company: String,
        val departureApt: String,
        val arrivalApt: String,
        val date: String,
        val gateClosingTime: String,
        val departureTime: String,
        val arrivalTime: String,
        val duration: String,
        val price: Double
    ) {
        fun toFlight(companies: List<Company>, airports: List<Airport>): Flight {
            val company = companies.find { it.name == company } ?:
                throw IllegalArgumentException("Unknown company")

            val departureApt = airports.find { it.code == departureApt } ?:
                throw IllegalArgumentException("Unknown airport")

            val arrivalApt = airports.find { it.code == arrivalApt } ?:
                throw IllegalArgumentException("Unknown airport")

            return Flight(
             id,
             company,
             departureApt,
             arrivalApt,
             date.toLocalDate(),
             gateClosingTime,
             departureTime,
             arrivalTime,
             duration,
             price
            )
        }
    }
}