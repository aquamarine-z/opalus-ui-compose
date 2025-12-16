package io.github.aquamarinez

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform