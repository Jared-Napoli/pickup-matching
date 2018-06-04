import java.io.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime


val reqRecipHeaderIndices = mutableMapOf(
        "Latitude" to -1,
        "Longitude" to -1,
        "Restrictions" to -1,
        "Sunday" to -1,
        "Monday" to -1,
        "Tuesday" to -1,
        "Wednesday" to -1,
        "Thursday" to -1,
        "Friday" to -1,
        "Saturday" to -1
    )

val reqCustHeaderIndices = mutableMapOf(
        "Latitude" to -1,
        "Longitude" to -1,
        "Categories" to -1,
        "PickupAt" to -1,
        "TimeZoneId" to -1
    )
var recipients = mutableListOf<Recipient>()
const val DEFAULT_TIMEZONE = "America/Los_Angeles"
const val RECIPIENT_FILE = "Recipients.csv"
const val CUSTOMER_FILE = "Customers.csv"
const val OUTPUT_FILE = "MatchScores.csv"
const val NUM_OF_FOOD_CATEGORIES = 8
const val MAXIMUM_DISTANCE = 10.0
var scores = mutableListOf<MatchScore>()

fun main(args: Array<String>) {
    var br : BufferedReader? = null
    var line : String?
    var lineTokens : List<String>
    var fw : FileWriter? = null
    var customerIndex: Int


    try {
        println("Reading from $RECIPIENT_FILE")
        br = BufferedReader(FileReader(RECIPIENT_FILE))
        recipients = getRecipientsFromCSV(br)

        println("Reading from $CUSTOMER_FILE")
        br = BufferedReader(FileReader(CUSTOMER_FILE))

        processHeaders(br, reqCustHeaderIndices)

        line = br.readLine()
        customerIndex = 0
        println("Writing to $OUTPUT_FILE")
        fw = FileWriter(OUTPUT_FILE)
        fw.appendln("CustomerCSVIndex,TopMatchCSVIndex,TopMatchScore,2ndBestMatchCSVIndex,2ndBestMatchScore,...")
        while (line != null) {
            lineTokens = line.split(",")
            val cust = Customer(
                    lineTokens[reqCustHeaderIndices["Latitude"]!!].toDouble(),      // set customer latitude
                    lineTokens[reqCustHeaderIndices["Longitude"]!!].toDouble(),     // set customer longitude
                    lineTokens[reqCustHeaderIndices["Categories"]!!].toShort(),     // set customer categories
                    lineTokens[reqCustHeaderIndices["PickupAt"]!!],                 // set customer pickup time
                    lineTokens[reqCustHeaderIndices["TimeZoneId"]!!]                // set customer timezone
            )

            var distance: Float
            var foodMatchCount: Int
            var pickupAvailable: Boolean
            for ((recipIndex, recip) in recipients.withIndex()) {

                //get distance
                distance = getDistance(cust.latitude, cust.longitude, recip.latitude, recip.longitude)

                // get count of food matches
                foodMatchCount = getFoodMatchCount(cust.categories, recip.restrictions)

                // get pickup availabilities
                pickupAvailable = getPickupAvailability(cust, recip)

                // only adds valid matches
                if (isValidMatch(foodMatchCount, distance, pickupAvailable)) {
                    scores.add(MatchScore(recipIndex, getScore(foodMatchCount, distance)))
                }
            }
            line = br.readLine()

            writeRowToCSV(fw, customerIndex, scores)

            customerIndex++     // move to next customer
            scores.clear()
        }

        fw.close()
        br.close()
        println("Done")
    }
    catch(e: NumberFormatException) {
        println("ERROR: Could not parse CSV file correctly")
        println(e.message)
    }
    catch(e: FileNotFoundException) {
        println("ERROR: File not found")
        println(e.message)
    }
    catch(e: IOException) {
        println("ERROR: Problem with IO")
        println(e.message)
    }
    finally {
        fw?.close()
        br?.close()
    }
}

// returns a list of Recipients obtained form the CSV
fun getRecipientsFromCSV(br: BufferedReader): MutableList<Recipient> {
    val recipList = mutableListOf<Recipient>()
    var line: String?
    var lineTokens: List<String>

    processHeaders(br, reqRecipHeaderIndices)

    // begin reading rest of file
    line = br.readLine()
    while(line != null) {
        lineTokens = line.split(",")
        recipList.add(Recipient(
                lineTokens[reqRecipHeaderIndices["Latitude"]!!].toDouble(),         // set recipient latitude
                lineTokens[reqRecipHeaderIndices["Longitude"]!!].toDouble(),        // set recipient longitude
                lineTokens[reqRecipHeaderIndices["Restrictions"]!!].toShort(),      // set recipient restrictions
                arrayOf(                                                            // sets the weekday values in
                        lineTokens[reqRecipHeaderIndices["Monday"]!!].toInt(),      // weekday array
                        lineTokens[reqRecipHeaderIndices["Tuesday"]!!].toInt(),
                        lineTokens[reqRecipHeaderIndices["Wednesday"]!!].toInt(),
                        lineTokens[reqRecipHeaderIndices["Thursday"]!!].toInt(),
                        lineTokens[reqRecipHeaderIndices["Friday"]!!].toInt(),
                        lineTokens[reqRecipHeaderIndices["Saturday"]!!].toInt(),
                        lineTokens[reqRecipHeaderIndices["Sunday"]!!].toInt()
                )
        ))
        line = br.readLine()
    }
    br.close()
    return recipList
}

// aggregates functions involved with processing the CSV headers
fun processHeaders(br: BufferedReader, reqHeaderIndices: MutableMap<String, Int>) {
    // read header
    val line = br.readLine()
    val lineTokens = line.split(",")
    if(!hasRequiredHeader(lineTokens, reqHeaderIndices)) { // ensures all necessary headers are included
        throw NumberFormatException("Missing one or more required headers")
    }
    hasRequiredHeader(lineTokens, reqHeaderIndices)
    setIndices(lineTokens, reqHeaderIndices) // sets the appropriate CSV header indices
}

// returns true if all of the required headers are found on the CSV file
fun hasRequiredHeader(headerTokens: List<String>, reqHeaders: MutableMap<String, Int>): Boolean {
    for(header in reqHeaders.keys) {
        if(header !in headerTokens)
            return false
    }
    return true
}

// write the sorted list of scores to a row in the output file
fun writeRowToCSV(fw: FileWriter, customerIndex: Int, scores: MutableList<MatchScore>) {
    fw.append("${customerIndex}")
    for(match in scores.sortedWith(compareByDescending({ it.score }))) {
        fw.append(",${match.csvIndex},${match.score}")
    }
    fw.append("\n")
}

// sets the indices of the required header according to order on the CSV file
fun setIndices(headerTokens: List<String>, reqHeaderMap: MutableMap<String, Int>) {
    for(key in reqHeaderMap.keys) {
        reqHeaderMap[key] = headerTokens.indexOf(key)
    }
}

// finds the distance between two coordinates, returns in miles
// adapted from https://dzone.com/articles/distance-calculation-using-3
fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val theta = lon1 - lon2
    var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
            Math.cos(deg2rad(theta))
    dist = Math.acos(dist)
    dist = rad2deg(dist)
    dist = dist * 60.0 * 1.1515
    return dist.toFloat()
}

// converts degrees to radians
// adapted from https://dzone.com/articles/distance-calculation-using-3
fun deg2rad(deg: Double): Double {
    return deg * Math.PI / 180.0
}

// converts radians to degrees
// adapted from https://dzone.com/articles/distance-calculation-using-3
fun rad2deg(rad: Double): Double {
    return rad * 180.0 / Math.PI
}

// returns the number of food items that match for a customer and recipient
fun getFoodMatchCount(custCategories: Short, recipRestrictions: Short): Int {
    var count = 0
    val categories = custCategories.toInt()
    val restrictions = recipRestrictions.toInt()

    (0..(NUM_OF_FOOD_CATEGORIES - 1)).forEach { i ->
        if(checkBit(categories, i) and !(checkBit(restrictions, i))) {
            count++
        }
    }
    return count
}

// checks the Integer int's bit at position pos, returns true if set, false if not
fun checkBit(int: Int, pos: Int): Boolean {
    if(pos < 0)
        return false
    return int and (1 shl pos) != 0
}

// determine if recipient is open at pickup time
fun getPickupAvailability(cust: Customer, recip: Recipient): Boolean {
    val pickupTime = LocalDateTime.parse(cust.pickupAt.substring(0, 19))
    val custZoneId = ZoneId.of(cust.timeZoneId)
    val defaultZoneID = ZoneId.of(DEFAULT_TIMEZONE)
    val zonedDateTime: ZonedDateTime = pickupTime.atZone(custZoneId)
    val adjustedDateTime:  ZonedDateTime =zonedDateTime.withZoneSameInstant(defaultZoneID)
    return checkBit(recip.weekdays[adjustedDateTime.dayOfWeek.value - 1], adjustedDateTime.hour - 8)
}

// determines if valid matches
// recipient must be less than 10 miles away
// recipient MUST be open at pickup time
// recipient must have at least one food item that can be received
fun isValidMatch(foodMatchCount: Int, distance: Float, availability:Boolean): Boolean {
    return foodMatchCount > 0 && distance < MAXIMUM_DISTANCE && availability
}

// Score prioritizes number of matching food items over distance, so foodMatchCount is weighted * 10
// higher score is better
fun getScore(foodMatchCount : Int, distance: Float): Float {
    return 10 * foodMatchCount - distance
}