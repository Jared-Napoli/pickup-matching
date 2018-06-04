import org.junit.Assert
import org.junit.Test

class PickupMatchingKtTest {

    //hasRequiredIndices tests
    @Test
    fun `hasRequiredIndices returns true if all headers are provided`() {
        val requiredHeaders = mutableMapOf("req" to 0, "also req" to 0, "req too" to 0)
        val givenHeaders = listOf("req", "also req", "not req", "extra", "req too")
        Assert.assertTrue(hasRequiredHeader(givenHeaders, requiredHeaders))
    }

    @Test
    fun `hasRequiredIndices returns false if missing a header`() {
        val requiredHeaders = mutableMapOf("req" to 0, "also req" to 0, "req too" to 0)
        val givenHeaders = listOf("req", "also req", "not req", "extra")
        Assert.assertFalse(hasRequiredHeader(givenHeaders, requiredHeaders))
    }

    //setIndices tests
    @Test
    fun `setIndices returns correctly sets the indices for all required headers`() {
        val requiredHeaders = mutableMapOf("req" to -1, "also req" to -1, "req too" to -1)
        val givenHeaders = listOf("also req", "req", "not req", "extra", "req too")
        setIndices(givenHeaders, requiredHeaders)
        Assert.assertEquals(requiredHeaders["req"], 1)
        Assert.assertEquals(requiredHeaders["also req"], 0)
        Assert.assertEquals(requiredHeaders["req too"], 4)
    }

    @Test
    fun `setIndices does not change headers that are not found`() {
        val requiredHeaders = mutableMapOf("req" to -1, "req not found" to -1, "req too" to -1)
        val givenHeaders = listOf("req", "not req", "extra", "req too")
        setIndices(givenHeaders, requiredHeaders)
        Assert.assertEquals(requiredHeaders["req not found"], -1)
    }

    @Test
    fun `setIndices works if given empty header list`() {
        val requiredHeaders = mutableMapOf("req" to -1, "also req" to -1, "req too" to -1)
        val givenHeaders = listOf<String>()
        setIndices(givenHeaders, requiredHeaders)
        Assert.assertEquals(requiredHeaders["req"], -1)
        Assert.assertEquals(requiredHeaders["also req"], -1)
        Assert.assertEquals(requiredHeaders["req too"], -1)
    }

    // checkBit tests
    @Test
    fun `checkBit correctly returns true when checking a 1 bit`() {
        val intToCheck = 8          // 1000
        val positionOfOneBit = 3
        Assert.assertTrue(checkBit(intToCheck, positionOfOneBit))
    }

    @Test
    fun `checkBit correctly returns false when checking a 0 bit`() {
        val intToCheck = 95         // 1011111
        val positionOfOneBit = 5
        Assert.assertFalse(checkBit(intToCheck, positionOfOneBit))
    }

    @Test
    fun `checkBit returns false when given a negative bit position`() {
        val intToCheck = 95         // 1011111
        val positionOfOneBit = -5
        Assert.assertFalse(checkBit(intToCheck, positionOfOneBit))
    }

    // getFoodMatchCount Tests
    @Test
    fun `getFoodMatchCount returns correct number of matches`() {
        val categoryToCheck: Short = 89    // 01011001
        val restrictionToCheck: Short = 44 // 00101100
        Assert.assertEquals(getFoodMatchCount(categoryToCheck, restrictionToCheck), 3)
    }

    @Test
    fun `getFoodMatchCount returns NUM_OF_FOOD_CATEGORIES as max number of matches`() {
        val categoryToCheck: Short = 511   // 111111111
        val restrictionToCheck: Short = 0 // 000000000
        Assert.assertEquals(getFoodMatchCount(categoryToCheck, restrictionToCheck), NUM_OF_FOOD_CATEGORIES)
    }

    @Test
    fun `getFoodMatchCount returns 0 when no matches are found`() {
        val categoryToCheck: Short = 255   // 11111111
        val restrictionToCheck: Short = 255 // 11111111
        Assert.assertEquals(getFoodMatchCount(categoryToCheck, restrictionToCheck), 0)
    }

    // getDistance tests
    @Test
    fun `getDistance returns correct distance`() {
        val lat1 = 32.9697
        val long1 = -96.80322
        val lat2 = 29.46786
        val long2 = -98.53506
        val result = getDistance(lat1, long1, lat2, long2)
        val sameLocationResult = getDistance(lat1, long1, lat1, long1)
        Assert.assertEquals(result, 262.6778.toFloat())
        Assert.assertEquals(sameLocationResult, 0.0.toFloat())
    }

    @Test
    fun `getPickupAvailability returns correct availability`() {
        // request for a pickup at 3:00pm on Sunday LA Time
        val testCustomer = Customer(-1.0, -1.0, 44, "2018-06-03T15:00:00-8:00", "America/Los_Angeles")
        // Only open on 3:00pm on Sundays
        val testRecipient = Recipient(-1.0, -1.0, 44, arrayOf(-1,-1,-1,-1,-1,-1,128))
        Assert.assertTrue(getPickupAvailability(testCustomer, testRecipient))
    }

    @Test
    fun `getPickupAvailability returns correct availability with differing timezones`() {
        // request for a pickup at 8:00pm on Monday in Sydney, Australia (converts to 3pm Sunday in LA)
        val testCustomer = Customer(-1.0, -1.0, 44, "2018-06-04T08:00:00+10:00", "Australia/Sydney")
        // Only open on 3:00pm on Sundays in LA time
        val testRecipient = Recipient(-1.0, -1.0, 44, arrayOf(-1,-1,-1,-1,-1,-1,128))
        Assert.assertTrue(getPickupAvailability(testCustomer, testRecipient))
    }

    @Test
    fun `getPickupAvailability returns false when not open at requested time availability with differing timezones`() {
        // request for a pickup at 8:00pm on Monday in Sydney, Australia (converts to 4pm Sunday in LA)
        val testCustomer = Customer(-1.0, -1.0, 44, "2018-06-04T09:00:00+10:00", "Australia/Sydney")
        // Only open on 3:00pm on Sundays in LA time
        val testRecipient = Recipient(-1.0, -1.0, 44, arrayOf(-1,-1,-1,-1,-1,-1,128))
        Assert.assertFalse(getPickupAvailability(testCustomer, testRecipient))
    }

    @Test
    fun `getPickupAvailability returns false for pickup requests before 8am`() {
        // request for a pickup at 3:00pm on Sunday LA Time
        val testCustomer = Customer(-1.0, -1.0, 44, "2018-06-03T07:00:00-8:00", "America/Los_Angeles")
        // Only open on 3:00pm on Sundays
        val testRecipient = Recipient(-1.0, -1.0, 44, arrayOf(-1,-1,-1,-1,-1,-1,128))
        Assert.assertFalse(getPickupAvailability(testCustomer, testRecipient))
    }

    // getScore test
    @Test
    fun `getScore always weights more food matches over distance`() {
        val foodCountBest = 2
        val distanceBest = 9.9999.toFloat()     // furthest possible distance
        val foodCountOther = 1
        val distanceOther = 0.0.toFloat()       // closest possible distance
        Assert.assertTrue(getScore(foodCountBest,distanceBest) > getScore(foodCountOther, distanceOther))
    }
}