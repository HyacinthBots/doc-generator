## Usage Guide

Doc-generator is very easy to use. Once configured, it will automatically update your documents file whenever you add
a new command, handy, right?

### Basic setup and configuration

First things first is to make sure you have added the dependency to your project. If you haven't, just pop over to the 
[README](/README.md) where you can find out how to do so.

Once the dependency has been added to your project, you'll want to navigate to your `main` function and find the 
`ExtensibleBot(TOKEN) {}` builder. Once you're there you can get started. You'll want to add the `docGenerator` function
in and set the `enabled` property to `true`. This tells doc-generator that we'd like to use it. If you need a hand on
what it should look like, there is an example below.

<details>
    <summary>Getting started</summary>
    
```kotlin
docGenerator {
    enabled = true
}
```
</details>

Now that Doc-gen knows we'd like to use it, we need to configure it a little to tune it to our liking.

The first thing you'll want to specify is the `fileFormat` for your documentation file. This option takes in an ordinal 
from `SupportedFileFormat`; Currently, the only support is for Markdown files, though further support will be worked on 
soon. I will use Markdown in this example.

<details>
    <summary>With file format option.</summary>

```kotlin
docGenerator {
    enabled = true
    fileFormat = SupportedFileFormat.MARKDOWN
}
```
</details>

Excellent, doc-gen now knows that we'd like to use it, and we'd like our documents to be output as Markdown. 
Now we better tell it where we'd like to output the file. This varies greatly per project, but you'll want something 
along the lines of `Path("./docs/commands.md")`, specified as our `filePath`.

<details>
    <summary>With file path option.</summary>

```kotlin
docGenerator {
    enabled = true
    fileFormat = SupportedFileFormat.MARKDOWN
    filePath = Path("./docs/commands.md")
}
```
</details>

So far we've told doc-gen, we'd like to use it and how we'd like our documents to be handled, only a couple more options
left to configure before we're ready to go! 

We need to let doc-gen know what command types we'd like documentation generated for. This field takes in an ordinal of
`CommandType`. The available options are: `SLASH`, `MESSAGE`, `USER` and `ALL`. You need to wrap the options you'd like
to enable in a `listOf()` function, however, if you are using `ALL` there is no need for this, as it is a list type 
itself. For this example, I'm going to generate documentation for `SLASH` commands.

<details>
    <summary>With command type option.</summary>

```kotlin
docGenerator {
    enabled = true
    fileFormat = SupportedFileFormat.MARKDOWN
    commandTypes = listOf(CommandType.SLASH)
    // commandTypes = CommandType.ALL  
}
```
</details>

The final mandatory option for doc-gen is the `environment`. This is necessary because doc-gen won't be able to generate
a documentation file whilst running in a production environment, due to it being run from a JAR, not a directory. To 
populate this option, it is recommended to use a `.env` file that specifies the `environment` property. The options that
must be provided are: `production` or `development`. If the option is neither of those strings, an 
`InvalidEnvironmentVariableException` will be thrown. If you feel an option should be added, please make an issue on the
[GitHub tracker](https://github.com/HyacinthBots/doc-generator).

<details>
    <summary>With environment option.</summary>

```kotlin
docGenerator {
    enabled = true
    fileFormat = SupportedFileFormat.MARKDOWN
    filePath = Path("./docs/commands.md")
    commandTypes = listOf(CommandType.SLASH)
    environment = env("ENVIRONMENT")
}
```
</details>

Great success! Doc-gen is now fully set up and ready for use. Simply run your bot one and the file will be generated at
startup.
If you'd like to do any more configuration, see [Further Configuration](#further-configuration) 

### Further configuration

Doc-gen comes with a couple of extra configuration options for you to specify. The first of these is 
`useBuiltinCommandList`. This option, when set to true, will add a slash command to your bot that presents a command 
list when run. This command will produce a paginated embed with every command in the project, providing it has been 
enabled in the `commandTypes`.

<details>
    <summary>With built in command option.</summary>

```kotlin
docGenerator {
    enabled = true
    fileFormat = SupportedFileFormat.MARKDOWN
    fielPath = Path("./docs/commands.md")
    commandTypes = listOf(CommandType.SLASH)
    environment = env("ENVIRONMENT")
    useBuiltinCommandList = true
}
```
</details>

Some bots may use translations in their projects, never fear, doc-gen comes with excellent native support for 
translations, meaning all you have to do is specify what languages to translate to, in the builder! Just add the 
`translationSupport` DSL to add your languages. The translation builder needs to know the name of the resource bundle
your translations are stored in. Provide this by settings `bundleName`.

<details>
    <summary>With translations option.</summary>

```kotlin
docGenerator {
    enabled = true
    fileFormat = SupportedFileFormat.MARKDOWN
    fielPath = Path("./docs/commands.md")
    commandTypes = listOf(CommandType.SLASH)
    environment = env("ENVIRONMENT")
    useBuiltinCommandList = true
    translationSupport {
        enableTranslations = true
        bundleName = "testbot"
        supportedLanguages = listOf(Locale.ENGLISH_GREAT_BRITAIN, Locale.GERMAN)
    }
}
```

It is also recommended that your `i18n` builder specifies the same languages as the doc-generator:
```kotlin
i18n {
    applicationCommandLocale(Locale.ENGLISH_GREAT_BRITAIN, Locale.GERMAN)
    interactionUserLocaleResolver()
}
```
</details>

### Providing extra documentation

Now doc-gen can only pull a limited amount of information by itself, sometimes it may need a little bit of aid in 
providing full documentation. Thankfully, doc-gen provides a nice way to do this. You'll find that inside a 
`*slashCommand` DSL that there are 2 new available builders, `additionalDocumentation` and 
`subCommandAdditionalDocumentation`. On the face of it these two seem completely identical, and you'd be absolutely 
correct in thinking that, they are functionally the same. The reason they both exist though is for, as their names 
specify, to differentiate between sub-commands and non sub-commands. Due to how the system for extra documentation works
if they were both the same, you'd find that every sub-command has the same set of documentation, regardless of whether
you've set documentation each one. 

With that out they way, usage is simple, there are 2 options. `commandResult` and `extraInformation`. Let's start with 
`commandResult`. As the name implies, here you can write about the result of running the command, or provide a 
translation key. When this field is populated it will be displayed in the commands list command and the commands file. 
Do note that this field will **not** be displayed on commands that have sub-commands. The reason for this, is that there
is the `subCommandAdditionalDocumentation` builder that can be populated to display the information there.

`extraInformation` is simply just a field where you can provide any information you'd like about the command, there 
isn't much else to it, actually.

<details>
    <summary>Example additional documentation usage.</summary>

```kotlin
publicSlashCommand(::SlapArguments) {
    name = "slap"
    description = "Slaps someone with a cold wet fish"

    additionalDocumentation {
        commandResult = "Announces you have slapped someone"
        extraInformation = "If you're a little irritated with someone, slap them with a fish!"
    }
    
    action {
        // ...
    }
}
```
</details>

## Support and contact

If you're facing any issues with doc-gen, navigate your way to the 
[HyacinthBots Discord Server](https://discord.gg/hy2329fcTZ) where we'll be able to help you out. If you've got any 
requests or issues, please open a ticket on [the issue tracker](https://github.com/HyacinthBots/doc-generator/issues).

