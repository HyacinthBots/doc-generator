import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Meta {
    const val PROJECT_VERSION = "0.3.2"
    const val DESCRIPTION = "Generate documentation for KordEx bots!"
    const val GITHUB_REPO = "HyacinthBots/doc-generator"
    const val RELEASE = "https://repo.kordex.dev/external-releases"
    const val SNAPSHOT = "https://repo.kordex.dev/external-snapshots"

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
    alias(libs.plugins.kordex.plugin)
    alias(libs.plugins.kordex.i18n)
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
        name = "Kord Snapshots"
        url = uri("https://snapshots.kord.dev")
    }

    maven {
        name = "Kord Extensions (Snapshots)"
        url = uri("https://snapshots-repo.kordex.dev")
    }

    maven {
        name = "Kord Extensions (Releases)"
        url = uri("https://releases-repo.kordex.dev")
    }

    maven {
        name = "Kord Extensions (Snapshots 2)"
        url = uri("https://repo.kordex.dev/snapshots")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.kotlin.stdlib)
    implementation(libs.logging)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(kotlin("test-junit5"))
    testRuntimeOnly(libs.junit.jupiter.engine)
}

kordEx {
    kordExVersion = "2.4.1-20251224.130127-1"
    ignoreIncompatibleKotlinVersion = true
}

i18n {
    bundle("doc-generator.strings", "docgenerator.i18n") {
        publicVisibility = false
    }
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "clean checkLegacyAbi applyLicenses detekt")
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
    rule(file(rootProject.file("HEADER")))
    include("**/*.kt", "**/*.java", "**/strings**.properties")
    exclude("**/Translations.kt")
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
                username = System.getenv("KORDEX_USER")
                password = System.getenv("KORDEX_PASSWORD")
            }
        }
    }
}
