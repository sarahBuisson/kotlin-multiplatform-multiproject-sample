package sample4

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    val name: String
}

fun hello(): String = "Hello from ${Platform.name}"