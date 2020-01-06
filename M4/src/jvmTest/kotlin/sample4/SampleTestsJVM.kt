package sample4

import kotlin.test.Test
import kotlin.test.assertTrue
import sample.hello
class SampleTestsJVM {
    @Test
    fun testHello() {
        assertTrue("JVM" in hello())
    }
}