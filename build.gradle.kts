import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
}

group = "com.github.nikvoloshin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.exposed", "exposed-core", "0.23.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.23.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.23.1")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.6.2")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.6.2")

    testImplementation("org.postgresql", "postgresql", "42.2.12")
    testImplementation("mysql", "mysql-connector-java", "8.0.19")
    testImplementation("com.microsoft.sqlserver", "mssql-jdbc", "8.2.2.jre8")

    testImplementation("org.testcontainers", "testcontainers", "1.12.0")
    testImplementation("org.testcontainers", "junit-jupiter", "1.12.0")
    testImplementation("org.testcontainers", "postgresql", "1.12.0")
    testImplementation("org.testcontainers", "mysql", "1.12.0")
    testImplementation("org.testcontainers", "mssqlserver", "1.12.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}