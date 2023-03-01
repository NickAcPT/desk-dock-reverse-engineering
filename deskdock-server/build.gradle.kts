plugins {
    id("desk-dock-reverse-engineering-plugin")
}

dependencies {
    deskdock("com.floriandraschbacher:deskdock:1.3.1:server")
}

workspace {
    intermediaryObfuscationPattern = ".+"
}