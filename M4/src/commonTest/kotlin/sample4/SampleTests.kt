package sample4

import kotlin.test.Test
import kotlin.test.assertTrue
import sample.Sample
class SampleTests {
    @Test
    fun testMe() {
        assertTrue(Sample().checkMe() > 0)
    }
}