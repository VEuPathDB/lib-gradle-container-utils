import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-gradle-plugin`
  `maven-publish`
  kotlin("jvm") version "1.9.10"
}

group = "org.veupathdb.lib"
version = "5.0.5"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17

  if (project.hasProperty("full-publish")) {
    withSourcesJar()
    withJavadocJar()
  }
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
  }
}

repositories {
  mavenLocal()
  mavenCentral()
}

gradlePlugin {
  // Define the plugin
  val `container-utils` by plugins.creating {
    id = "org.veupathdb.lib.gradle.container.container-utils"
    implementationClass = "org.veupathdb.lib.gradle.container.ContainerUtilsPlugin"
    description = "Utilities for building containerized services"
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")

  implementation(kotlin("stdlib"))
  implementation(kotlin("stdlib-jdk8"))

  implementation(platform("com.fasterxml.jackson:jackson-bom:2.15.3"))
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.core:jackson-annotations")
}

publishing {
  repositories {
    maven {
      name = "GitHub"
      url = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
    }
  }

  publications {
    create<MavenPublication>("gpr") {
      from(components["java"])

      pom {
        name.set("Gradle Container Build Utils")
        description.set("Provides functionality to assist the construction of containerized VEuPathDB gradle projects")
        url.set("https://github.com/VEuPathDB/lib-gradle-container-utils")
        developers {
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("epharper@upenn.edu")
            url.set("https://github.com/foxcapades")
            organization.set("VEuPathDB")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/VEuPathDB/lib-gradle-container-utils.git")
          developerConnection.set("scm:git:ssh://github.com/VEuPathDB/lib-gradle-container-utils.git")
          url.set("https://github.com/VEuPathDB/lib-gradle-container-utils")
        }
      }
    }
  }
}
