plugins {
    java
    kotlin("jvm") version "1.5.31"
    id("org.openapi.generator") version "5.4.0"
    id("maven-publish")
}

val mavenName = "api.hq"
group = "com.flocktory"
version = System.getenv("API_VERSION") ?: "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
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

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/spec/api-hq.yaml")
    outputDir.set("$buildDir/generated")
    apiPackage.set("$group.$mavenName.api")
    invokerPackage.set("$group.$mavenName.invoker")
    modelPackage.set("$group.$mavenName.model")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "delegatePattern" to "true",
        )
    )
}

publishing {
    repositories {
        maven {
            url = uri(System.getenv("MAVEN_REPOSITORY_LINK"))
            credentials(PasswordCredentials::class) {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}