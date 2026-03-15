pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // StarXpand SDK for Android
        maven { url = uri("https://packagecloud.io/StarMicronics/stario10-android-sdk/maven2") }
    }
}

rootProject.name = "sodatter-bt"
include(":app")
