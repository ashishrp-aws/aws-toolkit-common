import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject

val jacksonVersion = "2.10.0"
val junitVersion = "4.13"
val kotlinVersion = "1.3.20"
val assertjVersion = "3.12.0"

plugins {
    java
    `kotlin-dsl` version "1.1.3"
    kotlin("jvm") version "1.3.60"
    `maven-publish`
}

buildscript {
    repositories {
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        "classpath"(group = "com.github.everit-org.json-schema", name = "org.everit.json.schema", version = "1.12.1")
        classpath(kotlin("gradle-plugin", version = "1.3.20"))
    }
}

group = "software.aws.toolkits.telemetry.generator"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.squareup:kotlinpoet:1.5.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.github.everit-org.json-schema:org.everit.json.schema:1.12.1")
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
}

tasks {
    compileKotlin {
        dependsOn(":copyTelemetryResources", ":validatePackagedSchema")
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        dependsOn(":copyTestTelemetryResources")
        kotlinOptions.jvmTarget = "1.8"
    }
    register("validatePackagedSchema") {
        group = "build"
        description = "Validates that the packaged definition is compatable with the packaged schema"
        doFirst {
            try {
                val rawSchema = JSONObject(org.json.JSONTokener(File("src/main/resources/telemetrySchema.json").readText()))
                val schema: Schema = SchemaLoader.load(rawSchema)
                schema.validate(JSONObject(File("src/main/resources/commonDefinitions.json").readText()))
                schema.validate(JSONObject(File("src/main/resources/jetbrainsDefinitions.json").readText()))
            } catch (e: Exception) {
                println("Exception while validating packaged schema, ${e.printStackTrace()}")
            }
        }
    }
    task(name = "copyTelemetryResources", type = Copy::class) {
        from("../telemetrySchema.json", "../definitions/commonDefinitions.json", "../definitions/jetbrainsDefinitions.json")
        include("*.json")
        into("src/main/resources")
    }
    task(name = "copyTestTelemetryResources", type = Copy::class) {
        from("../telemetrySchema.json", "../definitions/commonDefinitions.json", "../definitions/jetbrainsDefinitions.json")
        include("*.json")
        into("src/test/resources")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}