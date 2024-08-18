/*
 * Copyright (c) 2022-2024 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import dev.kordex.core.commands.application.message.MessageCommand
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.commands.application.user.UserCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.ResourceBundleTranslations
import dev.kordex.core.i18n.SupportedLocales
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hyacinthbots.docgenerator.addArguments
import org.hyacinthbots.docgenerator.additionalDocumentation
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import org.hyacinthbots.docgenerator.exceptions.ConflictingFileFormatException
import org.hyacinthbots.docgenerator.findOrCreateDocumentsFile
import org.hyacinthbots.docgenerator.formatPermissionsSet
import org.hyacinthbots.docgenerator.subCommandAdditionalDocumentation
import org.hyacinthbots.docgenerator.translate
import java.io.IOException
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter

/**
 * The object that contains the document generation functions.
 */
internal object DocsGenerator {
	internal val generatorLogger = KotlinLogging.logger { }

	/**
	 * This function generates the contents of the markdown docs format.
	 *
	 * @param commandTypes A [List] of [CommandTypes] to document
	 * @param loadedExtensions A [List] of loaded [Extension]s from the bot
	 * @param language The [Locale] to translate into, or null
	 *
	 * @return A [String] containing all the documents
	 */
	private fun generateMarkdownContents(
		commandTypes: List<CommandTypes>,
		loadedExtensions: List<Extension>,
		language: Locale? = null
	): String {
		var totalOutput = ""
		val externalTranslationsProvider = ResourceBundleTranslations { language ?: SupportedLocales.ENGLISH }
		@Suppress("UNUSED_VALUE") // You do not understand
		commandTypes.forEach { type ->
			when (type) {
				CommandTypes.SLASH -> {
					// Gather a list of slash commands from the loaded extensions
					val slashCommands: MutableList<SlashCommand<*, *, *>> = mutableListOf()
					loadedExtensions.forEach { extension ->
						slashCommands.addAll(extension.slashCommands)
					}

					// Set a variable for the output
					var output = "## ${"title.slash".translate(externalTranslationsProvider, language)}\n\n"

					// Iterate through the list of slash commands we gathered
					if (slashCommands.isNotEmpty()) {
						for (slashCommand in slashCommands) {
							var commandInfo = "" // The eventual output of this particular slash command
							// If the slash command list is not empty, format the documents with sub command info
							if (slashCommand.subCommands.isNotEmpty()) {
								// For each sub command in the list, format it and add arguments
								slashCommand.subCommands.forEach { subCommand ->
									val subProvider = subCommand.translationsProvider
									val bundle = subCommand.extension.bundle
									var arguments = addArguments(subCommand, subProvider, bundle, language)
									// Get any additional documentation from the map
									var subExtraDocs = subCommand.subCommandAdditionalDocumentation[subCommand.name]

									// If the arguments list is empty, acknowledge it
									if (arguments.isEmpty()) {
										arguments = "arguments.none".translate(subProvider, language)
									}

									// Add the title and description of the sub command
									commandInfo += "### ${
										"header.command.name".translate(subProvider, language)
									}: `${subCommand.parentCommand?.name?.translate(subProvider, language, bundle)} ${
										subCommand.name.translate(subProvider, language, bundle)
									}`\n**${"header.command.description".translate(subProvider, language)}**: ${
										subCommand.description.translate(subProvider, language, bundle)
									}\n${
										// Add the result of the command, if one was provided
										if (subExtraDocs?.commandResult != null) {
											"\n**${"header.result".translate(subProvider, language)}**: ${
												subExtraDocs.commandResult!!.translate(subProvider, language, bundle)
											}\n"
										} else {
											""
										}
									}${
										// Add the additional info, if there was any provided
										if (subExtraDocs?.extraInformation != null) {
											"\n**${"header.additionalinfo".translate(subProvider, language)}**: ${
												subExtraDocs.extraInformation!!.translate(subProvider, language, bundle)
											}\n"
										} else {
											""
										}
									}${
										// Add any required bot perms to the document
										if (subCommand.requiredPerms.isNotEmpty()) {
											"\n**${"header.permissions.bot".translate(subProvider, language)}**:${
												subCommand.requiredPerms.formatPermissionsSet(language)
											}\n"
										} else {
											""
										}
									}${
										// Add any required member perms to the document
										if (subCommand.defaultMemberPermissions != null) {
											"\n**${"header.permissions.member".translate(subProvider, language)}**: ${
												subCommand.defaultMemberPermissions.formatPermissionsSet(language)
											}\n"
										} else {
											""
										}
										// Actually add the arguments
									}\n* **${"header.arguments".translate(subProvider, language)}**:\n$arguments\n---\n"
									subExtraDocs = null // Reset the extra docs to get the new ones on the next loop
								}
							} else {
								val slashProvider = slashCommand.translationsProvider // Quick var to allow inlining
								// Get the extra documents for the command from the map, if there are any
								var extraDocs = slashCommand.additionalDocumentation[slashCommand.name]
								val bundle = slashCommand.extension.bundle // Quick var to allow for inlining
								var arguments = addArguments(slashCommand, slashProvider, bundle, language)
								// If there are no arguments, acknowledge it
								if (arguments.isEmpty()) {
									arguments = "arguments.none".translate(slashProvider, language)
								}

								// Add the name and description to the doc
								commandInfo +=
									"### ${"header.command.name".translate(slashProvider, language)}: `${
										slashCommand.name.translate(slashProvider, language, bundle)
									}`\n**${"header.command.description".translate(slashProvider, language)}**: ${
										slashCommand.description.translate(slashProvider, language, bundle)
									}\n${
										// Add the result of the command if there is one
										if (extraDocs?.commandResult != null) {
											"\n**${"header.result".translate(slashProvider, language)}**: ${
												extraDocs.commandResult!!.translate(slashProvider, language, bundle)
											}\n"
										} else {
											""
										}
									}${
										// Add the extra information if there is any
										if (extraDocs?.extraInformation != null) {
											"\n**${"header.additionalinfo".translate(slashProvider, language)}**: ${
												extraDocs.extraInformation!!.translate(slashProvider, language, bundle)
											}\n"
										} else {
											""
										}
									}${
										// Add the required bot permissions if there are any
										if (slashCommand.requiredPerms.isNotEmpty()) {
											"\n**${"header.permissions.bot".translate(slashProvider, language)}**:${
												slashCommand.requiredPerms.formatPermissionsSet(language)
											}\n"
										} else {
											""
										}
									}${
										// Add the required member permissions if there are any
										if (slashCommand.defaultMemberPermissions != null) {
											"\n**${"header.permissions.member".translate(slashProvider, language)}**: ${
												slashCommand.defaultMemberPermissions.formatPermissionsSet(language)
											}\n"
										} else {
											""
										}
										// Add the arguments
									}\n* ${"header.arguments".translate(slashProvider, language)}:\n$arguments\n---\n"
								extraDocs = null // Reset the extra documents for it to be gotten in the next loop
							}

							output += commandInfo // Add the command info to the output
						}
					} else {
						output += "arguments.none".translate(externalTranslationsProvider, language) + "\n\n---\n"
					}

					totalOutput += output // Add the slash info to the total output
				}

				CommandTypes.MESSAGE -> {
					// Collect all the message commands into a list from the loaded extensions
					val messageCommands: MutableList<MessageCommand<*, *>> = mutableListOf()
					loadedExtensions.forEach { extension ->
						messageCommands.addAll(extension.messageCommands)
					}

					var output = "## ${"title.message".translate(externalTranslationsProvider, language)}\n\n"

					// Loop through the collected message commands
					if (messageCommands.isNotEmpty()) {
						for (messageCommand in messageCommands) {
							// Get the additional docs from the map, if there are any
							var additionalDocs = messageCommand.additionalDocumentation[messageCommand.name]
							val provider = messageCommand.translationsProvider // Quick variable to help inlining
							val bundle = messageCommand.extension.bundle // Quick variable to help inlining
							output +=
									// Add the name of the command to the list. Message commands have no description :(
								"### ${"header.messagecommand.name".translate(provider, language)}: `${
									messageCommand.name.translate(provider, language, bundle)
								}`\n${
									// Add the provided command result, if there is one
									if (additionalDocs?.commandResult != null) {
										"\n**${"header.result".translate(provider, language)}**: ${
											additionalDocs.commandResult!!.translate(provider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									// Add the provided extra info if there is any
									if (additionalDocs?.extraInformation != null) {
										"\n**${"header.additionalinfo".translate(provider, language)}**: ${
											additionalDocs.extraInformation!!.translate(provider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									// Add the required bot perms, if there are any
									if (messageCommand.requiredPerms.isNotEmpty()) {
										"\n**${"header.permissions.bot".translate(provider, language)}**:${
											messageCommand.requiredPerms.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}${
									// Add the required member permissions, if there are any
									if (messageCommand.defaultMemberPermissions != null) {
										"\n**${"header.permissions.member".translate(provider, language)}**: ${
											messageCommand.defaultMemberPermissions.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}\n---\n"
							additionalDocs = null // Reset the additional docs for next time
						}
					} else {
						output += "${"arguments.none".translate(externalTranslationsProvider, language)}\n\n---\n"
					}

					totalOutput += output // Add message commands to the total output
				}

				CommandTypes.USER -> {
					// Collect the user commands into a list
					val userCommands: MutableList<UserCommand<*, *>> = mutableListOf()
					loadedExtensions.forEach { extension ->
						userCommands.addAll(extension.userCommands)
					}

					var output = "## ${"title.user".translate(externalTranslationsProvider, language)}\n\n"

					if (userCommands.isNotEmpty()) {
						// Loop through all the user commands
						for (userCommand in userCommands) {
							// Get the additional docs from the map, if there are any
							var additionalDocs = userCommand.additionalDocumentation[userCommand.name]
							val provider = userCommand.translationsProvider // Quick variable to aid inlining
							val bundle = userCommand.extension.bundle // Quick variable to aid inlining
							output +=
									// Add the name and info, user commands have no description :(
								"### ${"header.usercommand.name".translate(provider, language)}: `${
									userCommand.name.translate(provider, language, userCommand.bundle)
								}\n${
									// Add the command result if there is any
									if (additionalDocs?.commandResult != null) {
										"\n**${"header.result".translate(provider, language)}**: ${
											additionalDocs.commandResult!!.translate(provider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									// Add the extra information if there is any
									if (additionalDocs?.extraInformation != null) {
										"\n**${"header.additionalinfo".translate(provider, language)}**: ${
											additionalDocs.extraInformation!!.translate(provider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									// Add the required bot permissions if any
									if (userCommand.requiredPerms.isNotEmpty()) {
										"\n**${"header.permissions.bot".translate(provider, language)}**:${
											userCommand.requiredPerms.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}${
									// Add the required member permissions if any
									if (userCommand.defaultMemberPermissions != null) {
										"\n**${"header.permissions.member".translate(provider, language)}**: ${
											userCommand.defaultMemberPermissions.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}\n---\n"
							additionalDocs = null // Reset the additional docs for next time
						}
					} else {
						output += "arguments.none".translate(externalTranslationsProvider, language) + "\n\n---\n"
					}

					totalOutput += output // Add user commands to the total output
				}
			}
		}

		return totalOutput
	}

	/**
	 * Updates the document file.
	 *
	 * @param path The path to the document file
	 * @param fileFormat The [SupportedFileFormat] to store the file in
	 * @param commandTypes The [List] of support [CommandTypes]
	 * @param loadedExtensions The [List] of [Extension]s
	 * @param languages A [List] of [Locale]s to translate the docs into
	 */
	suspend inline fun updateDocumentsFile(
		path: Path,
		fileFormat: SupportedFileFormat,
		commandTypes: List<CommandTypes>,
		loadedExtensions: List<Extension>,
		languages: List<Locale>? = null
	) {
		// If the languages list isn't null or empty, create the specific language files for each on
		if (!languages.isNullOrEmpty()) {
			languages.forEach { language ->
				// Get a translated path
				val i18nPath = Path(path.toString().replace(".md", "-${language.toLanguageTag()}.md"))
				findOrCreateDocumentsFile(i18nPath) // Find and/or create the file

				// Check permissions and extension, combust if there are issues.
				if (!i18nPath.toFile().canRead() || !i18nPath.toFile().canWrite()) {
					throw IOException(
						"Unable to read/write documents file for ${language.toLanguageTag()}! Please check the permissions"
					)
				} else if (i18nPath.toFile().extension != fileFormat.fileExtension) {
					throw ConflictingFileFormatException(fileFormat.fileExtension, path.toFile().extension)
				}
			}
		} else {
			findOrCreateDocumentsFile(path) // Find and/or create the file

			// Check permissions and extension, combust if there are issues
			if (!path.toFile().canRead() || !path.toFile().canWrite()) {
				throw IOException("Unable to read/write documents file! Please check the permissions!")
			} else if (path.toFile().extension != fileFormat.fileExtension) {
				throw ConflictingFileFormatException(fileFormat.fileExtension, path.toFile().extension)
			}
		}

		@Suppress("OptionalWhenBraces") // How about no
		when (fileFormat) {
			SupportedFileFormat.MARKDOWN -> {
				// If there is anything in the language list
				if (!languages.isNullOrEmpty()) {
					languages.forEach { language ->
						// Generate the contents in the target language
						val contents = generateMarkdownContents(commandTypes, loadedExtensions, language)
						// Get the translated path
						val translatedPath = Path(path.toString().replace(".md", "-${language.toLanguageTag()}.md"))
						val writer = translatedPath.bufferedWriter(Charsets.UTF_8) // Create a writer
						withContext(Dispatchers.IO) {
							writer.write("") // Clear the old contents
							writer.write(contents) // Add the new contents
							writer.flush()
							writer.close()
						}
						generatorLogger.info { "Written documents for ${language.toLanguageTag()}!" }
					}
				} else {
					// Generate the contents in the target language
					val contents = generateMarkdownContents(commandTypes, loadedExtensions)
					val writer = path.bufferedWriter(Charsets.UTF_8) // Create a writer
					withContext(Dispatchers.IO) {
						writer.write("") // Clear the old contents
						writer.write(contents) // Add the new contents
						writer.flush()
						writer.close()
					}
					generatorLogger.info { "Written documents!" }
				}
			}
		}
	}
}
