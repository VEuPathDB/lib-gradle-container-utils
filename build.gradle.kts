import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `java-gradle-plugin`
  `maven-publish`
  alias(libs.plugins.kotlin)
  alias(libs.plugins.dokka)
}

group = "org.veupathdb.lib"
version = "6.3.0"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17

  if (project.hasProperty("full-publish")) {
    withSourcesJar()
    withJavadocJar()
  }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

repositories {
  mavenLocal()
  mavenCentral()
}

gradlePlugin {
  // Define the plugin
  val containerUtils by plugins.creating {
    id = "org.veupathdb.lib.gradle.container.container-utils"
    implementationClass = "org.veupathdb.lib.gradle.container.ContainerUtilsPlugin"
    description = "Utilities for building containerized services"
  }
}

dependencies {
  implementation(libs.gradle.kotlin)
  implementation(libs.jackson.databind)
  implementation(libs.jackson.annotations)
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
            email.set("epharper.vpdb@foxcapades.io")
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

testing {
  suites {
    withType<JvmTestSuite> {
      useJUnitJupiter(libs.versions.junit)
      dependencies {
        implementation(libs.mockito.core)
        implementation(libs.mockito.junit)
      }
    }
  }
}

val updateReadme = tasks.register("update-readme") {
  doLast {
    ProcessBuilder("sed", "s/^:p-version:.\\+/:p-version: ${project.version}/")
  }
}

tasks.withType<PublishToMavenRepository> { finalizedBy(updateReadme) }
