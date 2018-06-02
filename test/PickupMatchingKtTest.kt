import org.junit.Assert
import org.junit.Assert.*;
import org.junit.Test

class PickupMatchingKtTest {

    //hasRequiredIndices tests
    @Test
    fun `hasRequiredIndices returns false if missing a header`() {
        val requiredHeaders = mutableMapOf("req" to 0, "also req" to 0, "req too" to 0)
        val givenHeaders = listOf("req", "also req", "not req", "extra")
        Assert.assertFalse(hasRequiredHeader(givenHeaders, requiredHeaders))
    }

    @Test
    fun `hasRequiredIndices returns true if all headers are provided`() {
        val requiredHeaders = mutableMapOf("req" to 0, "also req" to 0, "req too" to 0)
        val givenHeaders = listOf("req", "also req", "not req", "extra", "req too")
        Assert.assertTrue(hasRequiredHeader(givenHeaders, requiredHeaders))
    }

    //setIndices tests
    @Test
    fun `setIndices returns correctly sets the indices for all required headers`() {
        val requiredHeaders = mutableMapOf("req" to 0, "also req" to 0, "req too" to 0)
        val givenHeaders = listOf("also req", "req", "not req", "extra", "req too")
        setIndices(givenHeaders, requiredHeaders)
        Assert.assertEquals(requiredHeaders.get("req"), 1)
        Assert.assertEquals(requiredHeaders.get("also req"), 0)
        Assert.assertEquals(requiredHeaders.get("req too"), 4)
    }
}