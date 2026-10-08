pluginManagement {
    includeBuild("build-logic")
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
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "CherryPokemon"
include(":app")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:model")
include(":core:navigation")
include(":core:network")
include(":core:testing")
include(":core:ui")
include(":feature:pokemondetail:api")
include(":feature:pokemondetail:impl")
include(":feature:pokemonlist:api")
include(":feature:pokemonlist:impl")
