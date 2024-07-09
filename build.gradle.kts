import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("maven-publish")
}

group = "com.alttd"
version = "1.0.0-SNAPSHOT"
description = "Altitude Villager Shop plugin."

apply<JavaLibraryPlugin>()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories{
        maven {
            name = "alttd"
            url = uri("https://repo.destro.xyz/snapshots")
            credentials(PasswordCredentials::class)
        }
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}.jar")
        manifest {
            attributes("Main-Class" to "VillagerUI")
        }
    }

    build {
        dependsOn(shadowJar)
    }

}

dependencies {
    compileOnly("com.alttd:Galaxy-API:1.21-R0.1-SNAPSHOT") {
        isChanging = true
    }
    compileOnly("com.github.milkbowl:VaultAPI:1.7") {
        exclude("org.bukkit","bukkit")
    }
    compileOnly("com.alttd.datalock:api:1.1.0-SNAPSHOT")
}