import java.io.BufferedReader
import java.io.FileReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.io.FileWriter


val reqRecipHeaderIndices = mutableMapOf(
        "Latitude" to 0,
        "Longitude" to 0,
        "Restrictions" to 0,
        "Sunday" to 0,
        "Monday" to 0,
        "Tuesday" to 0,
        "Wednesday" to 0,
        "Thursday" to 0,
        "Friday" to 0,
        "Saturday" to 0
    )

val reqCustHeaderIndices = mutableMapOf(
        "Latitude" to 0,
        "Longitude" to 0,
        "Categories" to 0,
        "PickupAt" to 0,
        "TimeZoneId" to 0
    )
var recipients = mutableListOf<Recipient>()
const val DEFAULT_TIMEZONE = "America/Los_Angeles"
const val DEFAULT_RECIPIENT_FILE = "Recipients.csv"
const val DEFAULT_CUSTOMER_FILE = "Customers.csv"
const val DEFAULT_OUTPUT_FILE = "MatchScores.csv"
var scores = mutableListOf<MatchScore>()

fun main(args: Array<String>) {
    var br : BufferedReader?
    var line : String?
    var lineTokens : List<String>
    var fw = FileWriter(DEFAULT_OUTPUT_FILE)
    var customerIndex: Int

    br = BufferedReader(FileReader(DEFAULT_RECIPIENT_FILE))
    recipients = getRecipientsFromCSV(br)

    println("Reading from $DEFAULT_CUSTOMER_FILE")
    br = BufferedReader(FileReader(DEFAULT_CUSTOMER_FILE))

    processHeaders(br, reqCustHeaderIndices)

    line = br.readLine()
    customerIndex = 0
    println("Writing to $DEFAULT_OUTPUT_FILE")
    fw.appendln("CustCSVIndex,TopMatchCSVIndex,TopMatchScore,2ndBestMatchCSVIndex,2ndBestMatchScore,...")
    while(line != null) {
        lineTokens = line.split(",")
        val cust = Customer(
                lineTokens[reqCustHeaderIndices["Latitude"]!!].toDouble(),
                lineTokens[reqCustHeaderIndices["Longitude"]!!].toDouble(),
                lineTokens[reqCustHeaderIndices["Categories"]!!].toShort(),
                lineTokens[reqCustHeaderIndices["PickupAt"]!!],
                lineTokens[reqCustHeaderIndices["TimeZoneId"]!!]
        )

        var distance: Float
        var foodMatchCount: Int
        var pickupAvailable: Boolean
        for((recipIndex, recip) in recipients.withIndex()) {

            //get distance
            distance = getDistance(cust.latitude, cust.longitude, recip.latitude, recip.longitude)

            // get count of food matches
            foodMatchCount = getFoodMatchCount(cust.categories, recip.restrictions)

            // get pickup availabilities
            pickupAvailable = getPickupAvailability(cust, recip)

            // only add valid matches
            // recipient must be less than 10 miles away
            // recipient MUST be open at pickup time
            // recipient must have at least one food item that can be received
            if(distance < 10.0 && pickupAvailable && foodMatchCount > 0) {
                scores.add(MatchScore(recipIndex, getScore(foodMatchCount, distance)))
            }
        }
        line = br.readLine()

        // write the sorted list of scores to a row in the output file
        fw.append("${customerIndex}")
        for(match in scores.sortedWith(compareByDescending({ it.score }))) {
            fw.append(",${match.csvIndex},${match.score}")
        }
        fw.append("\n")

        customerIndex++     // move to next customer
        scores.clear()
    }

    fw.close()
    br.close()
    println("Done")
}

fun getRecipientsFromCSV(br: BufferedReader): MutableList<Recipient> {
    val recipList = mutableListOf<Recipient>()
    var line: String?
    var lineTokens: List<String>

    println("Reading from $DEFAULT_RECIPIENT_FILE")

    processHeaders(br, reqRecipHeaderIndices)

    // begin reading rest of file
    line = br.readLine()
    while(line != null) {
        lineTokens = line.split(",")
        recipList.add(Recipient(
                lineTokens[reqRecipHeaderIndices["Latitude"]!!].toDouble(),         // set latitude
                lineTokens[reqRecipHeaderIndices["Longitude"]!!].toDouble(),        // set longitude
                lineTokens[reqRecipHeaderIndices["Restrictions"]!!].toShort(),      // set restrictions
                arrayOf(                                                                // sets the weekday values in
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

fun processHeaders(br: BufferedReader, reqHeaderIndices: MutableMap<String, Int>) {
    // read header
    val line = br.readLine()
    val lineTokens = line.split(",")
    if(!hasRequiredHeader(lineTokens, reqHeaderIndices)) { // ensures all necessary headers are included
        // handle issue
    }
    setIndices(lineTokens, reqHeaderIndices) // sets the appropriate CSV header indices
}

fun hasRequiredHeader(headerTokens: List<String>, reqHeaders: MutableMap<String, Int>): Boolean {
    for(header in reqHeaders.keys) {
        if(header !in headerTokens)
            return false
    }
    return true
}

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
    var count: Int = 0
    val categories = custCategories.toInt()
    val restrictions = recipRestrictions.toInt()

    (0..7).forEach { i ->
        if(testBit(categories, i) and !(testBit(restrictions, i))) {
            count++
        }
    }
    return count
}

// tests the Integer int's bit at position pos, returns true if set, false if not
fun testBit(int: Int, pos: Int): Boolean {
    return int and (1 shl pos) !== 0
}


// DAyofWeek indices are off by one
// may need to catch array out of bounds
fun getPickupAvailability(cust: Customer, recip: Recipient): Boolean {
    val pickupTime = LocalDateTime.parse(cust.pickupAt.substring(0, 19))
    val custZoneId = ZoneId.of(cust.timeZoneId)
    val defaultZoneID = ZoneId.of(DEFAULT_TIMEZONE)
    val zonedDateTime: ZonedDateTime = pickupTime.atZone(custZoneId)
    val adjustedDateTime:  ZonedDateTime =zonedDateTime.withZoneSameInstant(defaultZoneID)
    return testBit(recip.weekdays[adjustedDateTime.dayOfWeek.value - 1], adjustedDateTime.hour - 8)
}

// Score prioritizes number of matching food items over distance, so foodMatchCount is weighted * 10
// higher score is better
fun getScore(foodMatchCount : Int, distance: Float): Float {
    return 10 * foodMatchCount - distance
}
