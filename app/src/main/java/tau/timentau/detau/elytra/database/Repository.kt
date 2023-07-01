package tau.timentau.detau.elytra.database

import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import tau.timentau.detau.elytra.model.Accomodation
import tau.timentau.detau.elytra.model.AccomodationCategory
import tau.timentau.detau.elytra.model.Airport
import tau.timentau.detau.elytra.model.Booking
import tau.timentau.detau.elytra.model.City
import tau.timentau.detau.elytra.model.Company
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.model.PassengerData
import tau.timentau.detau.elytra.model.PaymentCircuit
import tau.timentau.detau.elytra.model.PaymentMethod
import tau.timentau.detau.elytra.model.ServiceClass
import tau.timentau.detau.elytra.model.ServiceClass.BUSINESS
import tau.timentau.detau.elytra.model.ServiceClass.ECONOMY
import tau.timentau.detau.elytra.model.ServiceClass.FIRST_CLASS
import tau.timentau.detau.elytra.model.Sex
import tau.timentau.detau.elytra.model.Ticket
import tau.timentau.detau.elytra.model.User
import tau.timentau.detau.elytra.toDateString
import java.util.Date

object Repository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val parser = Gson()

    suspend fun userExists(email: String, password: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
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

            val userAvatar = DatabaseDAO.getImage(userDTO.avatar)

            userDTO.toUser(userAvatar)
        }
    }

    suspend fun isMailUsed(email: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
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
            DatabaseDAO.selectPrimitiveValue<String>("""
                SELECT security_questions.question as string
                FROM users JOIN security_questions on users.question = security_questions.id
                WHERE users.email = '$email' 
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun isAnswerCorrect(email: String, answer: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
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
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
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
                WHERE id <> 1
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
        val dbId = id + 2 // gli indici generati partono da 1, e salta il primo che è di default
        DatabaseDAO.update("""
            UPDATE users
            SET avatar = $dbId
            WHERE email = '$email'
        """)
    }

    suspend fun changeEmail(oldEmail: String, newEmail: String) {
        DatabaseDAO.update("""
            UPDATE users
            SET email = '$newEmail'
            WHERE email = '$oldEmail'
        """)
        println("updated")
    }

    suspend fun getPaymentMethods(email: String): Deferred<List<PaymentMethod>> {
        return coroutineScope.async {
            val paymentMethodDTOs = DatabaseDAO.selectList<PaymentMethodDTO>("""
                SELECT *
                FROM payment_methods
                WHERE userEmail = '$email'
            """)

            val circuits = getPaymentCircuits().await()


            paymentMethodDTOs.map {
                // ottieni il circuito completo della carta
                val circuit = circuits.find { circuit -> circuit.name == it.circuit } ?:
                throw IllegalArgumentException("Unknown circuit")

                it.toPaymentMethod(circuit)
            }
        }
    }

    suspend fun getPaymentCircuits(): Deferred<List<PaymentCircuit>> {
        return coroutineScope.async {
            // ottieni i dati dei circuiti
            val circuitsDTOs = DatabaseDAO.selectList<PaymentCircuitDTO>(
                """
                    SELECT *
                    FROM payment_circuits
                """
            )

            val circuits = circuitsDTOs.map {
                val logo = DatabaseDAO.getImage(it.logo)
                it.toPaymentCircuit(logo)
            }

            circuits
        }
    }

    suspend fun removePaymentMethod(number: String) {
        DatabaseDAO.remove("""
            DELETE
            FROM payment_methods
            WHERE number = '$number'
        """)
    }

    suspend fun createPaymentMethod(
        email: String,
        number: String,
        circuit: PaymentCircuit,
        expiryDate: LocalDate,
        safetyCode: String,
        ownerFullName: String
    ) {
        // rimuovi gli spazi
        val cardNumber = number.replace(Regex("\\s"), "")

        DatabaseDAO.insert("""
            INSERT
            INTO payment_methods(number, userEmail, circuit, expiryDate, safetyCode, ownerFullName)
            VALUE ('$cardNumber', '$email', '${circuit.name}', '${expiryDate.toDateString()}',
                '$safetyCode', '$ownerFullName')
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

            val paths = companyDtos.map { it.logo }
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
        economy: Boolean,
        business: Boolean,
        firstClass: Boolean,
        selectedCompanies: List<String>): Deferred<List<Flight>> {

         return coroutineScope.async {
             if (!(economy || business || firstClass))
                throw IllegalArgumentException("Cannot fetch flights: no service class provided")

             // ottieni prima le informazioni sulle compagnie
             val companies = getCompanies().await()

             // ottieni le informazioni sugli aeroporti
             val airports = getAirports().await()

             val (minPrice, maxPrice) = priceRange

             val flights = mutableListOf<Flight>()

             // aggiungi i voli richiesti
             if (economy) {
                 val flightDTOs = getFlightsForServiceClass(
                         departureApt,
                         arrivalApt,
                         date,
                         minPrice,
                         maxPrice,
                         passengersCount,
                         "economy",
                         selectedCompanies
                 )

                 flights.addAll(
                     flightDTOs.map { it.toFlight(companies, airports, ECONOMY) }
                 )
             }

             if (business) {
                 val flightDTOs = getFlightsForServiceClass(
                     departureApt,
                     arrivalApt,
                     date,
                     minPrice,
                     maxPrice,
                     passengersCount,
                     "business",
                     selectedCompanies
                 )

                 flights.addAll(
                     flightDTOs.map { it.toFlight(companies, airports, BUSINESS) }
                 )
            }

             if (firstClass) {
                 val flightDTOs = getFlightsForServiceClass(
                     departureApt,
                     arrivalApt,
                     date,
                     minPrice,
                     maxPrice,
                     passengersCount,
                     "firstClass",
                     selectedCompanies
                 )

                 flights.addAll(
                     flightDTOs.map { it.toFlight(companies, airports, FIRST_CLASS) }
                 )
            }

             flights
        }
    }

    private suspend fun getFlightsForServiceClass(
        departureApt: String,
        arrivalApt: String,
        date: LocalDate,
        minPrice: Double,
        maxPrice: Double,
        passengersCount: Int,
        serviceClass: String,
        selectedCompanies: List<String>
    ): List<FlightDTO> {

        val companiesSectionBuilder = StringBuilder()
        selectedCompanies.forEach { companiesSectionBuilder.append("'$it', ") }

        val companiesStr =
            if (companiesSectionBuilder.isEmpty())
                ""
            else
                "AND f.company IN (${companiesSectionBuilder.removeSuffix(", ")})"

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
                ffs.${serviceClass}Free >= $passengersCount $companiesStr
            """)
    }

    suspend fun getCities(): Deferred<List<City>> {
        return coroutineScope.async {
            DatabaseDAO.selectList<City>("""
                SELECT *
                FROM cities
            """)
        }
    }

    suspend fun getAccomodations(
        city: String,
        minPrice: Double,
        maxPrice: Double,
        rating: Int,
        selectedCategories: List<String>
    ): Deferred<List<Accomodation>> {

        val categoriesSectionBuilder = StringBuilder()
        selectedCategories.forEach {categoriesSectionBuilder.append("'$it', ")}

        val categoriesStr =
            if (categoriesSectionBuilder.isEmpty())
                ""
            else
                "AND a.category IN (${categoriesSectionBuilder.removeSuffix(", ")})"

        return coroutineScope.async {
            val accomodationDTOs = DatabaseDAO.selectList<AccomodationDTO>("""
                SELECT a.id, a.name, a.description, a.category, c.name as city, 
                    a.address, a.image, a.price, a.rating
                FROM accomodations a JOIN cities c on a.city = c.id
                WHERE c.name = '$city' AND
                    a.price BETWEEN $minPrice AND $maxPrice AND
                    a.rating >= $rating $categoriesStr
            """)

            val accomodations = mutableListOf<Accomodation>()
            accomodationDTOs.forEach {
                val image = DatabaseDAO.getImage(it.image)
                accomodations.add(it.toAccomodation(image))
            }

            accomodations
        }
    }

    suspend fun insertBooking(
        email: String,
        accomodation: Accomodation,
        checkInDate: LocalDate,
        checkOutDate: LocalDate,
        hostCount: Int,
        nightCount: Int,
        price: Double,
    ) {
        DatabaseDAO.insert(
            """
            INSERT INTO bookings(user, accomodation, checkInDate, checkOutDate, 
                hostCount, nightCount, price)
            VALUE ('$email', '${accomodation.id}', '${checkInDate.toDateString()}',
            '${checkOutDate.toDateString()}', $hostCount, $nightCount, $price)
        """
        )
    }

    suspend fun getTickets(email: String): Deferred<List<Ticket>> {
        return coroutineScope.async {
            val ticketsDTOs = DatabaseDAO.selectList<TicketDTO>(
                """
            SELECT id, flight, serviceClass, passengersCount, passengersInfo, price, makingDate
            FROM tickets
            WHERE user = '$email'
        """
            )

            val tickets = mutableListOf<Ticket>()

            for (ticketDTO in ticketsDTOs) {
                val flight = getFlight(ticketDTO.flight, ticketDTO.serviceClass)

                tickets.add(ticketDTO.toTicket(flight))
            }

            tickets
        }

    }

    suspend fun insertTicket(
        email: String,
        flight: Flight,
        passengersCount: Int,
        passengerData: List<PassengerData>,
        price: Double,
    ) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val json = parser.toJson(passengerData)

        DatabaseDAO.insert(
            """
            INSERT INTO tickets(user, flight, serviceClass, passengersCount, passengersInfo,
                price, makingDate)
            VALUES ('$email', '${flight.id}', '${flight.serviceClass}', $passengersCount,
                '$json', $price, '${today.toDateString()}')
        """
        )
    }

    suspend fun recustomizeTicket(
        id: Int,
        passengerData: List<PassengerData>,
        addonPrice: Double,
    ) {
        val json = parser.toJson(passengerData)

        DatabaseDAO.update(
            """
            UPDATE tickets
            SET passengersInfo = '$json', price = price + $addonPrice
            WHERE id = $id
        """
        )
    }

    private suspend fun getFlight(id: String, serviceClass: ServiceClass): Flight {
        // ottieni prima le informazioni sulle compagnie
        val companies = getCompanies().await()

        // ottieni le informazioni sugli aeroporti
        val airports = getAirports().await()

        val flightDTO = DatabaseDAO.selectValue<FlightDTO>(
            """
            SELECT f.id, f.company, f.departureApt, f.arrivalApt, f.date, f.gateClosingTime,
                f.departureTime, f.arrivalTime, f.duration, 
                fp.${serviceClass.name.lowercase()}Price as price
            FROM flights f JOIN flights_prices fp ON f.id = fp.id
            WHERE f.id = '$id'
        """
        )

        return flightDTO!!.toFlight(companies, airports, serviceClass)
    }

    suspend fun getBookings(email: String): Deferred<List<Booking>> {
        return coroutineScope.async {
            val bookingsDTO = DatabaseDAO.selectList<BookingDTO>(
                """
                SELECT b.id, b.user, b.accomodation, b.checkInDate, b.checkOutDate, 
                    b.hostCount, b.nightCount, b.price
                FROM bookings b JOIN accomodations a on a.id = b.accomodation
                WHERE b.user = '$email'
            """
            )

            val bookings = mutableListOf<Booking>()

            bookingsDTO.forEach {
                val accomodation = getAccomodation(it.accomodation)

                bookings.add(it.toBooking(accomodation))
            }

            bookings
        }
    }

    private suspend fun getAccomodation(id: String): Accomodation {
        val accomodationDTO = DatabaseDAO.selectValue<AccomodationDTO>(
            """
            SELECT a.id, a.name, a.description, a.category, c.name as city, 
                a.address, a.image, a.price, a.rating
            FROM accomodations a JOIN cities c on a.city = c.id
            WHERE a.id = '$id'
        """
        )

        val image = DatabaseDAO.getImage(accomodationDTO!!.image)

        return accomodationDTO.toAccomodation(image)
    }

    private class UserDTO(
        val email: String,
        val fullName: String,
        val birthDate: Date,
        val sex: Sex,
        val avatar: String,
        val password: String,
        val question: String,
        val answer: String,
    ) {
        fun toUser(avatar: Bitmap) =
            User(
                email,
                fullName,
                birthDate,
                sex,
                password.length,
                avatar
            )
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
        fun toFlight(
            companies: List<Company>,
            airports: List<Airport>,
            serviceClass: ServiceClass,
        ): Flight {
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
                date,
                gateClosingTime,
                departureTime,
                arrivalTime,
                duration,
                price,
                serviceClass
            )
        }
    }

    private class PaymentMethodDTO(
        val number: String,
        val userEmail: String,
        val circuit: String,
        val expiryDate: Date,
        val safetyCode: String,
        val ownerFullName: String
    ) {
        fun toPaymentMethod(circuit: PaymentCircuit) =
            PaymentMethod(
                number,
                circuit,
                expiryDate,
                safetyCode,
                ownerFullName
            )
    }

    private class PaymentCircuitDTO(
        val name: String,
        val logo: String,
        val startDigit: String
    ) {
        fun toPaymentCircuit(logo: Bitmap) =
            PaymentCircuit(
                name,
                logo,
                startDigit[0]
            )
    }

    private data class AccomodationDTO(
        val id: String,
        val name: String,
        val description: String,
        val category: AccomodationCategory,
        val city: String,
        val address: String,
        val image: String,
        val price: Double,
        val rating: Double,
    ) {
        fun toAccomodation(image: Bitmap) =
            Accomodation(id, name, description, category, city, address, image, price, rating)
    }

    private data class TicketDTO(
        val id: Int,
        val flight: String,
        val serviceClass: ServiceClass,
        val passengersCount: Int,
        val passengersInfo: String,
        val price: Double,
        val makingDate: String,
    ) {
        fun toTicket(flight: Flight): Ticket {
            val typeToken = TypeToken.getParameterized(List::class.java, PassengerData::class.java)
            val passengersData: List<PassengerData> =
                parser.fromJson(passengersInfo, typeToken.type)

            // si può ripersonalizzare se non son passati 3 giorni
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val ticketDate = LocalDate.parse(makingDate)

            val isRecustomizable = today < ticketDate.plus(3, DateTimeUnit.DAY)

            return Ticket(
                id,
                flight,
                passengersCount,
                passengersData,
                price,
                makingDate,
                isRecustomizable
            )
        }
    }

    private data class BookingDTO(
        val id: Int,
        val email: String,
        val accomodation: String,
        val checkInDate: String,
        val checkOutDate: String,
        val hostCount: Int,
        val nightCount: Int,
        val price: Double,
    ) {
        fun toBooking(accomodation: Accomodation) =
            Booking(id, accomodation, checkInDate, checkOutDate, hostCount, nightCount, price)
    }
}