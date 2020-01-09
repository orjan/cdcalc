import com.github.cdcalc.gradle.CalculateVersionTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}

plugins {
    base
    kotlin("jvm") version "1.3.61"

    id("com.github.cdcalc") version "0.0.21"
    id("com.github.ben-manes.versions") version "0.20.0" apply false

    id("com.dorongold.task-tree") version "1.3.1"
}

allprojects {
    group = "com.github.cdcalc"
}

subprojects {
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.kotlin.jvm")


    repositories {
        jcenter()
        mavenLocal()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.11")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0-M1")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.0-M1")
        testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.0-M1")
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        val jacocoTestReport = named<JacocoReport>("jacocoTestReport") {
            reports {
                xml.isEnabled = true
                html.isEnabled = true
            }
        }

        check {
            dependsOn(jacocoTestReport)
        }
    }
}

tasks {
    val calculateVersion = named<CalculateVersionTask>("calculateVersion") {
        doLast {
            val version = extra.get("version") as String
            project.findProject(":core")!!.tasks.named("bintrayUpload") {
                setProperty("versionName", version)
                setProperty("versionVcsTag", "v$version")
            }
        }
    }

    val releaseSetup by registering {
        dependsOn(calculateVersion)
    }

    val releaseBuild by registering {
        mustRunAfter(releaseSetup)
        dependsOn(":core:build", ":plugin:build", ":cli:build")
    }

    val releasePublish by registering {
        mustRunAfter(releaseBuild)
        dependsOn(":core:bintrayUpload", ":plugin:publishPlugins")
    }

    val releasePublishLocal by registering {
        mustRunAfter(releaseBuild)
        dependsOn(":core:publishToMavenLocal", ":plugin:publishToMavenLocal", ":cli:publishToMavenLocal")
    }

    register("release") {
        dependsOn(releaseSetup, releaseBuild, releasePublish)
    }

    register("releaseLocal") {
        dependsOn(releaseSetup, releaseBuild, releasePublishLocal)
    }
}

project(":plugin") {
    dependencies {
        implementation(project(":core"))
    }
}

project(":cli") {
    dependencies {
        implementation(project(":core"))
    }
}

tasks.wrapper {
    this.gradleVersion = "6.0.1"
    this.distributionType = Wrapper.DistributionType.ALL
}
