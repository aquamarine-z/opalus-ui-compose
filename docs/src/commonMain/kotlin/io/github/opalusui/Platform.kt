package io.github.opalusui

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun downloadFile(filename: String, content: String, mimeType: String = "text/html")