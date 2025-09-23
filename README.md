## Doc-Generator
Doc-generator is a small library utility for the [KordEx](https://github.com/Kord-Extensions/kord-extensions) library 
that automatically generates documentation for commands within your KordEx bot.

This project is licensed under the [MIT License](https://mit-license.org/)

---

### Translation Status

<a href="https://hosted.weblate.org/engage/lilybot/">
<img src="https://hosted.weblate.org/widget/lilybot/doc-generator/287x66-black.png" alt="Translation status" />
</a>

You can contribute to the translations via [this link](https://hosted.weblate.org/engage/lilybot/)

Translations are hosted on [weblate](https://hosted.weblate.org)

---


## Installation

### Latest version

#### Maven:

```xml
<!-- Adding the Releases repository -->
<repository>
    <id>releases-repo</id>
    <url>https://releases-repo-external.kordex.dev</url>
    <releases>
        <enabled>true</enabled>
    </releases>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>
```

```xml
<!-- Adding the Snapshots repository (Optional) -->
<repository>
    <id>snapshots-repo</id>
    <url>https://snapshots-repo-external.kordex.dev</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
```xml
<!-- Adding the dependency. Replace TAG with the latest version -->
<dependency>
    <groupId>org.hyacinthbots</groupId>
    <artifactId>doc-generator</artifactId>
    <version>TAG</version>
</dependency>
```

#### Gradle (Groovy)

```groovy
repositories {
    maven {
        name "KordEx External Releases"
        url "https://releases-repo-external.kordex.dev"
    }
    // Optionally add the snapshots repository
    maven {
        name "KordEx External Snapshots"
        url "https://snapshots-repo-external.kordex.dev"
    }
}

// Adding the dependency. Replace TAG with the latest version
dependencies {
    implementation('org.hyacinthbots:doc-generator:TAG')
}
```

#### Gradle (Kotlin)
```kotlin
repositories {
    maven {
        name = "KordEx External Releases"
        url = uri("https://releases-repo-external.kordex.dev")
    }
    // Optionally add the snapshots repository
    maven {
        name = "KordEx External Snapshots"
        url = uri("https://snapshots-repo-external.kordex.dev")
    }
}

// Adding the dependency. Replace TAG with latest version
dependencies {
    implementation("org.hyacinthbots:doc-generator:TAG")
}
```

### Getting started
Once the dependency has been added, navigate to the [usage guide](/docs/usage-guide.md) for how to get started.
