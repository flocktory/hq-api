import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.openapi.generator") version "5.4.0"
    id("maven-publish")
    id("com.diffplug.spotless") version "6.4.2"
}

val type = GenerationType.valueOf(project.properties.getOrDefault("type", "Unknown") as String)

enum class GenerationType {
    Server,
    Client,
    Unknown
}

val artifactName = when (type) {
    GenerationType.Server -> "hq.server"
    GenerationType.Client -> "hq.client"
    GenerationType.Unknown -> "hq.unknown"
}
val groupName = "com.flocktory.api"
val versionValue = System.getenv("API_VERSION") ?: "unknown"
val generateJavaClient = "generateJavaClient"
val generateServerStub = "generateServerStub"
val spotlessJavaApply = "spotlessJavaApply"
val prepareVersion = "prepareVersion"

group = groupName
version = versionValue

repositories {
    mavenCentral()
}

dependencies {
    if (type == GenerationType.Server) {
        implementation("org.springframework.boot:spring-boot-starter-web:2.6.2")
        implementation("org.springframework.data:spring-data-commons:2.6.0")
        implementation("org.springdoc:springdoc-openapi-ui:1.6.4")
        implementation("com.google.code.findbugs:jsr305:3.0.2")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
        implementation("org.openapitools:jackson-databind-nullable:0.2.2")
        implementation("org.springframework.boot:spring-boot-starter-validation:2.6.2")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    }
    if (type == GenerationType.Client) {
        implementation("javax.annotation:javax.annotation-api:1.3.2")
        implementation("io.swagger:swagger-annotations:1.5.24")
        implementation("org.openapitools:openapi-generator:5.4.0")
        implementation("com.squareup.okhttp3:okhttp:4.9.1")
        implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
        implementation("com.google.code.gson:gson:2.8.6")
        implementation("io.gsonfire:gson-fire:1.8.4")
    }
}

tasks.register(prepareVersion) {
    copy {
        from(layout.projectDirectory.file("spec/api-hq.yaml"))
        into(layout.projectDirectory.dir("spec/temp"))
        filter {
            it.replace("API_VERSION", versionValue)
        }
    }
    delete {
        file("$rootDir/spec/api-hq.yaml")
    }
    copy {
        from(layout.projectDirectory.file("spec/temp/api-hq.yaml"))
        into(layout.projectDirectory.dir("spec"))
    }
    delete("$rootDir/spec/temp")
}

tasks.register(generateServerStub, GenerateTask::class) {
    group = "openapi tools"
    generatorName.set("spring")
    groupId.set(groupName)
    inputSpec.set("$rootDir/spec/api-hq.yaml")
    outputDir.set("$buildDir/generated")
    apiPackage.set("$groupName.$artifactName.service")
    invokerPackage.set("$groupName.$artifactName.invoker")
    modelPackage.set("$groupName.$artifactName.model")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "delegatePattern" to "true",
        )
    )
    doLast {
        copy {
            from(layout.buildDirectory.dir("generated/src/main"))
            into(layout.projectDirectory.dir("src/main"))
        }
    }
}
tasks.getByPath(generateServerStub).dependsOn(prepareVersion)
tasks.getByPath(generateServerStub).finalizedBy(spotlessJavaApply)

tasks.register(generateJavaClient, GenerateTask::class) {
    group = "openapi tools"
    groupId.set(groupName)
    generatorName.set("java")
    inputSpec.set("$rootDir/spec/api-hq.yaml")
    outputDir.set("$buildDir/generated")
    apiPackage.set("$groupName.$artifactName.service")
    invokerPackage.set("$groupName.$artifactName.invoker")
    modelPackage.set("$groupName.$artifactName.model")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8"
        )
    )
    doLast {
        copy {
            from(layout.buildDirectory.dir("generated/src/main"))
            into(layout.projectDirectory.dir("src/main"))
        }
        copy {
            from(layout.buildDirectory.dir("generated/docs"))
            into(layout.projectDirectory.dir("src/resources/docs"))
        }
        copy {
            from(layout.buildDirectory.file("generated/api/openapi.yaml"))
            into(layout.projectDirectory.dir("src/resources"))
        }
    }
}
tasks.getByPath(generateJavaClient).dependsOn(prepareVersion)
tasks.getByPath(generateJavaClient).finalizedBy(spotlessJavaApply)

spotless {
    java {
        googleJavaFormat("1.8").aosp().reflowLongStrings()
        removeUnusedImports()
        importOrder()
        endWithNewline()
        trimTrailingWhitespace()
        indentWithSpaces()
    }
}

publishing {
    repositories {
        maven {
            url = uri(System.getenv("MAVEN_REPOSITORY_LINK") ?: "unknown")
            credentials(PasswordCredentials::class) {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = artifactName
            from(components["java"])
        }
    }
}
