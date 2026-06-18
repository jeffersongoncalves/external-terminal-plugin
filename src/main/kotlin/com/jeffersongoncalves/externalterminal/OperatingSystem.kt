package com.jeffersongoncalves.externalterminal

/** Detected host operating system. Kept as an enum so providers can branch on it
 *  and unit tests can drive each branch without touching real system properties. */
enum class OperatingSystem {
    WINDOWS,
    MAC,
    LINUX,
    UNKNOWN;

    companion object {
        fun current(): OperatingSystem = fromName(System.getProperty("os.name"))

        fun fromName(osName: String?): OperatingSystem {
            val name = osName?.lowercase() ?: return UNKNOWN
            return when {
                name.contains("win") -> WINDOWS
                name.contains("mac") || name.contains("darwin") -> MAC
                name.contains("nux") || name.contains("nix") || name.contains("aix") -> LINUX
                else -> UNKNOWN
            }
        }
    }
}
