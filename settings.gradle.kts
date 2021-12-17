rootProject.name = "VillagerShopUI"

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.destro.xyz/snapshots") // Altitude - Galaxy
        maven("https://jitpack.io") { // Vault
            content { includeGroup("com.github.milkbowl") }
        }
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
