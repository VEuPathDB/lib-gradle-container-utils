plugins {
  `java-gradle-plugin`
  `maven-publish`
}

group = "org.veupathdb.lib"
version = "1.0.10"

java {
  sourceCompatibility = JavaVersion.VERSION_14
  targetCompatibility = JavaVersion.VERSION_14
}

repositories {
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

//// Add a source set for the functional test suite
//val functionalTestSourceSet = sourceSets.create("functionalTest") {
//}

//gradlePlugin.testSourceSets(functionalTestSourceSet)
//configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
//
//// Add a task to run the functional tests
//val functionalTest by tasks.registering(Test::class) {
//  testClassesDirs = functionalTestSourceSet.output.classesDirs
//  classpath = functionalTestSourceSet.runtimeClasspath
//  useJUnitPlatform()
//}
//
//tasks.check {
//  // Run the functional tests as part of `check`
//  dependsOn(functionalTest)
//}
//
//tasks.test {
//  // Use JUnit Platform for unit tests.
//  useJUnitPlatform()
//}
