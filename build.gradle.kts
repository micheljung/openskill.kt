plugins {
  val kotlinVersion = "1.8.10"
  kotlin("jvm") version kotlinVersion
  `java-library`
  `maven-publish`
  signing
}

val projectVersion: String by project

group = "io.github.micheljung"
version = project.version
description = "Kotlin implementation of Openskill."

repositories {
  mavenCentral()
}

java {
  withSourcesJar()
  withJavadocJar()
}

val publicationName = "kotlin"

publishing {
  repositories {
    maven {
      name = "OSSRH"
      setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = System.getenv("OSSRH_USERNAME")
        password = System.getenv("OSSRH_TOKEN")
      }
    }
  }

  publications {
    create<MavenPublication>(publicationName) {
      groupId = "io.github.micheljung"
      artifactId = "openskill"

      from(components["kotlin"])
      artifact(tasks["sourcesJar"])
      artifact(tasks["javadocJar"])

      pom {
        name.set("Suirwik Components")
        description.set(project.description)
        url.set("https://github.com/micheljung/suirwik")
        licenses {
          license {
            name.set("MIT License")
            url.set("http://www.opensource.org/licenses/mit-license.php")
          }
        }
        developers {
          developer {
            id.set("mj")
            name.set("Michel Jung")
            email.set("michel.jung89@gmail.com")
          }
        }
        scm {
          connection.set("scm:git:https://github.com/micheljung/openskill.kt.git")
          developerConnection.set("scm:git:https://github.com/micheljung/openskill.kt.git")
          url.set("https://github.com/micheljung/openskill.kt")
        }
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications[publicationName])
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
}
