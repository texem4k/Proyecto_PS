package software.ulpgc.code

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform