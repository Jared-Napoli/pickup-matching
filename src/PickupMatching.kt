import java.io.BufferedReader
import java.io.FileReader


val reqRecipHeaderIndices = mutableMapOf<String, Int>("Latitude" to 0, "Longitude" to 0, "Restrictions" to 0,
        "Sunday" to 0, "Monday" to 0, "Tuesday" to 0, "Wednesday" to 0, "Thursday" to 0, "Friday" to 0, "Saturday" to 0)
val reqCustHeaderIndices = mutableMapOf<String, Int>("Latitude" to 0, "Longitude" to 0, "Categories" to 0, "PickupAt" to 0,
        "TimeZoneId" to 0)
var recipients = mutableListOf<Recipient>()
var debug: Boolean = true

fun main(args: Array<String>) {
    println("Reading from Recipients.csv:")
    var fileReader : BufferedReader? = null
    fileReader = BufferedReader(FileReader("Recipients.csv"))
    val recipHeader = fileReader.readLine()
    val recipHeaderTokens = recipHeader.split(",")

    if(debug) println(hasRequiredHeader(recipHeaderTokens, reqRecipHeaderIndices))

    setIndices(recipHeaderTokens, reqRecipHeaderIndices)

    if(debug) println(reqRecipHeaderIndices.toString())
    var line = fileReader.readLine()
    var lineTokens: List<String>
    while(line != null) {
        lineTokens = line.split(",")
        recipients.add(Recipient(
                lineTokens[reqRecipHeaderIndices.get("Latitude")!!].toDouble(),
                lineTokens[reqRecipHeaderIndices.get("Longitude")!!].toDouble(),
                lineTokens[reqRecipHeaderIndices.get("Restrictions")!!].toShort(),
                lineTokens[reqRecipHeaderIndices.get("Sunday")!!].toInt(),
                lineTokens[reqRecipHeaderIndices.get("Monday")!!].toInt(),
                lineTokens[reqRecipHeaderIndices.get("Tuesday")!!].toInt(),
                lineTokens[reqRecipHeaderIndices.get("Wednesday")!!].toInt(),
                lineTokens[reqRecipHeaderIndices.get("Thursday")!!].toInt(),
                lineTokens[reqRecipHeaderIndices.get("Friday")!!].toInt(),
                lineTokens[reqRecipHeaderIndices.get("Saturday")!!].toInt()
        ))
        line = fileReader.readLine()
    }
    if(debug) {
        println()
        println("printing Recipients")
        for(recip in recipients) {
            println(recip.toString())
        }
        println()
        println()
    }

    println("Reading from 'Customers.csv'")
    fileReader = BufferedReader(FileReader("Customers.csv"))

    val custHeader = fileReader.readLine()
    val custHeaderTokens = custHeader.split(",")

    if(debug) println(hasRequiredHeader(custHeaderTokens, reqCustHeaderIndices))

    setIndices(custHeaderTokens, reqCustHeaderIndices)

    if(debug) println(reqCustHeaderIndices.toString())
    line = fileReader.readLine()
    lineTokens = line.split(",")
    val cust: Customer = Customer(
            lineTokens[reqCustHeaderIndices.get("Latitude")!!].toDouble(),
            lineTokens[reqCustHeaderIndices.get("Longitude")!!].toDouble(),
            lineTokens[reqCustHeaderIndices.get("Categories")!!].toShort(),
            lineTokens[reqCustHeaderIndices.get("PickupAt")!!],
            lineTokens[reqCustHeaderIndices.get("TimeZoneId")!!]
            )

    if(debug) println(cust.toString())


    if(debug) println("done")
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
