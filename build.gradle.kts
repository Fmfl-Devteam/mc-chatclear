plugins { java }
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
