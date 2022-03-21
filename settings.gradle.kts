pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

includeBuild("convention-plugins")
include(":kowet")
include(":demo")
rootProject.name = "Kowet"