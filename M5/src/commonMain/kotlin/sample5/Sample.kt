package sample5

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    val name: String
}

public fun hello5(): String = "Hello from ${Platform.name}"