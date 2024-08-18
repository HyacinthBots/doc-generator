import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Meta {
    const val PROJECT_VERSION = "0.2.1"
    const val DESCRIPTION = "Generate documentation for KordEx bots!"
    const val GITHUB_REPO = "HyacinthBots/doc-generator"
    const val RELEASE = "https://s01.oss.sonatype.org/content/repositories/releases/"
    const val SNAPSHOT = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

    val version: String
        get() {
            val tag = System.getenv("GITHUB_TAG_NAME")
            val branch = System.getenv("GITHUB_BRANCH_NAME")
            return when {
                !tag.isNullOrBlank() -> tag
                !branch.isNullOrBlank() && branch.startsWith("refs/heads/") ->
                    "$PROJECT_VERSION-SNAPSHOT"

                else -> "undefined"
            }
        }

    val isSnapshot: Boolean get() = version.endsWith("-SNAPSHOT")
    val isRelease: Boolean get() = !isSnapshot && !isUndefined
    private val isUndefined: Boolean get() = version == "undefined"
}

plugins {
    `java-library`
    `maven-publish`
    signing

    alias(libs.plugins.kotlin)

    alias(libs.plugins.detekt)
    alias(libs.plugins.git.hooks)
    alias(libs.plugins.licenser)
    alias(libs.plugins.binary.compatibility.validator)
}

group = "org.hyacinthbots"
version = Meta.PROJECT_VERSION

val javaVersion = 13  // KordEx minimum pinned Java version

repositories {
    mavenCentral()

    maven {
        name = "Sonatype Snapshots (Legacy)"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Kord Extensions (Snapshots)"
        url = uri("https://snapshots-repo.kordex.dev")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kordex)

    implementation(libs.logging)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(kotlin("test-junit5"))
    testRuntimeOnly(libs.junit.jupiter.engine)
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "clean apiCheck updateLicense detekt")
    )
}

kotlin {
    explicitApi()
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
            languageVersion.set(KotlinVersion.fromVersion(libs.plugins.kotlin.get().version.requiredVersion.substringBeforeLast(".")))
            incremental = true
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isDeprecation = true
        options.release.set(javaVersion)

	    sourceCompatibility = javaVersion.toString()
	    targetCompatibility = javaVersion.toString()
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}

detekt {
    buildUponDefaultConfig = true
    config.from(files("$rootDir/detekt.yml"))

    autoCorrect = true
}

license {
    setHeader(rootProject.file("HEADER"))
    include("**/*.kt", "**/*.java", "**/strings**.properties")
}

signing {
    val signingKey = providers.environmentVariable("GPG_SIGNING_KEY")
    val signingPass = providers.environmentVariable("GPG_SIGNING_PASS")

    if (signingKey.isPresent && signingPass.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPass.get())
        val extension = extensions.getByName("publishing") as PublishingExtension
        sign(extension.publications)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = Meta.version
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set(project.name)
                description.set(Meta.DESCRIPTION)
                url.set("https://github.com/${Meta.GITHUB_REPO}")

                organization {
                    name.set("HyacinthBots")
                    url.set("https://github.com/HyacinthBots")
                }

                developers {
                    developer {
                        name.set("The HyacinthBots team")
                    }
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/${Meta.GITHUB_REPO}/issues")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org/")
                    }
                }

                scm {
                    url.set("https://github.com/${Meta.GITHUB_REPO}.git")
                    connection.set("scm:git:git://github.com/${Meta.GITHUB_REPO}.git")
                    developerConnection.set("scm:git:git://github.com/#${Meta.GITHUB_REPO}.git")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(if (Meta.isSnapshot) Meta.SNAPSHOT else Meta.RELEASE)

            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
}
