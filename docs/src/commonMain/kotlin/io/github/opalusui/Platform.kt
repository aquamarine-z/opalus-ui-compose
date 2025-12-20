package io.github.opalusui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform