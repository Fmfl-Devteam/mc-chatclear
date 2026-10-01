plugins {
    java
    checkstyle
    id("com.diffplug.spotless") version "8.10.3"
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.36.1").aosp()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
checkstyle {
    toolVersion = "14.3.0"
    maxWarnings = 0
}

group = "de.fmfl.devteam"
version = "1.0.0"
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.129-stable")
    testImplementation("io.papermc.paper:paper-api:26.2.build.129-stable")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.20.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8"; options.compilerArgs.add("-Xlint:deprecation") }
val pluginVersion = version.toString()
tasks.processResources {
    filteringCharset = "UTF-8"
    inputs.property("pluginVersion", pluginVersion)
    filesMatching("plugin.yml") { expand("version" to pluginVersion) }
}
tasks.test { useJUnitPlatform() }
