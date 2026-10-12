package io.github.opalusui


class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun downloadFile(filename: String, content: String, mimeType: String) {
    try {
        val file = java.io.File(filename)
        file.writeText(content)
    } catch (_: Exception) {}
}