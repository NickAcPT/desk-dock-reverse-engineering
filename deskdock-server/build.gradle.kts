plugins {
    id("desk-dock-reverse-engineering-plugin")
}

dependencies {
    deskdock("com.floriandraschbacher:deskdock:1.3.1:server")

    enigma("cuchaz:enigma-swing:2.3.0")
    enigmaDep("net.fabricmc:name-proposal:0.1.4")
}

workspace {
    intermediaryObfuscationPattern = ".+"
}