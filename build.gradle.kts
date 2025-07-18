plugins {
    id("java")
    id("io.freefair.lombok") version "8.11"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "com.mongenscave"
version = "1.0.7"

repositories {
    maven {
        name = "MonGens-Cave"
        url = uri("https://repo.mongenscave.com/")

        credentials {
            username = project.findProperty("mongensUsername") as String
            password = project.findProperty("mongensPassword") as String
        }
    }

    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.36")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

val apiJar = tasks.register<Jar>("apiJar") {
    archiveBaseName.set("mc-ChatSetup")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())

    from(sourceSets.main.get().output) {
        include("com/mongenscave/mcchatsetup/**")
    }
}

publishing {
    publications {
        create<MavenPublication>("apiJar") {
            artifact(apiJar.get()) {
                classifier = null
            }

            groupId = "com.mongenscave"
            artifactId = "mc-ChatSetup"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "MonGens-Cave"
            url = uri("https://repo.mongenscave.com/releases")
            credentials {
                username = project.findProperty("mongensUsername") as String
                password = project.findProperty("mongensPassword") as String
            }
        }
    }
}

tasks.register("deployApi") {
    dependsOn("apiJar", "publishApiJarPublicationToMonGens-CaveRepository")
}