/*
 * Copyright (c) 2022-2024 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.extensions

import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.commands.application.ApplicationCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.pagination.PublicResponsePaginator
import dev.kordex.core.pagination.pages.Page
import dev.kordex.core.pagination.pages.Pages
import org.hyacinthbots.docgenerator.addArguments
import org.hyacinthbots.docgenerator.additionalDocumentation
import org.hyacinthbots.docgenerator.builder.DocAdditionBuilder
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.formatPermissionsSet
import org.hyacinthbots.docgenerator.subCommandAdditionalDocumentation
import org.hyacinthbots.docgenerator.translate

public class CommandList(private val botName: String?, private val enabledCommands: List<CommandTypes>) : Extension() {
	override val name: String = "command-list"

	override suspend fun setup() {
		val pagesObj = Pages()

		bot.extensions.values.toMutableList().forEach {
			if (enabledCommands.contains(CommandTypes.SLASH)) {
				it.slashCommands.forEach { slashCommand ->
					if (slashCommand.subCommands.isNotEmpty()) {
						slashCommand.subCommands.forEach { subCommand ->
							val provider = subCommand.translationsProvider
							val bundle = subCommand.extension.bundle
							var arguments: String? = addArguments(subCommand, provider, bundle, null)
							if (arguments?.isEmpty() == true) arguments = null
							var subExtraDocs = subCommand.subCommandAdditionalDocumentation[subCommand.name]
							pagesObj.addPage(
								Page {
									title = "${subCommand.parentCommand?.name?.translate(provider, null, bundle)}" +
											" ${subCommand.name.translate(provider, null, bundle)}"
									description = subCommand.description.translate(provider, null, bundle)
									createEmbed(arguments, subCommand.requiredPerms, provider, bundle, subExtraDocs)
								}
							)
							subExtraDocs = null
						}
					} else {
						var arguments: String?
						val provider = slashCommand.translationsProvider
						val bundle = slashCommand.extension.bundle
						arguments = addArguments(slashCommand, provider, bundle, null)
						if (arguments.isEmpty() == true) arguments = null
						var extraDocs = slashCommand.additionalDocumentation[slashCommand.name]
						pagesObj.addPage(
							Page {
								title = slashCommand.name.translate(provider, null, bundle)
								description = slashCommand.description.translate(provider, null, bundle)
								createEmbed(arguments, slashCommand.requiredPerms, provider, bundle, extraDocs)
							}
						)
						extraDocs = null
					}
				}
			}

			if (enabledCommands.contains(CommandTypes.MESSAGE)) {
				it.messageCommands.forEach { messageCommand ->
					createMessageUserCommandDocs(messageCommand, pagesObj)
				}
			}

			if (enabledCommands.contains(CommandTypes.USER)) {
				it.userCommands.forEach { userCommand ->
					createMessageUserCommandDocs(userCommand, pagesObj)
				}
			}
		}

		publicSlashCommand {
			name = "command-list"
			description = "Shows a list of ${botName ?: kord.getSelf().username}'s commands!"

			action {
				val paginator = PublicResponsePaginator(
					pages = pagesObj,
					keepEmbed = true,
					owner = event.interaction.user,
					timeoutSeconds = 500,
					locale = this.getLocale(),
					interaction = interactionResponse
				)

				paginator.send()
			}
		}
	}

	/**
	 * Builds the embed content for the command list.
	 *
	 * @param args The command arguments string
	 * @param requiredPerms The required permissions to run the command
	 * @param provider The translation provider
	 * @param bundle The bundle to get the translations from
	 * @param extraDocs Any extra documentation for the command
	 */
	private fun EmbedBuilder.createEmbed(
		args: String?,
		requiredPerms: MutableSet<Permission>,
		provider: TranslationsProvider,
		bundle: String?,
		extraDocs: DocAdditionBuilder?
	) {
		field {
			name = "Arguments"
			value = args ?: "None"
		}
		field {
			name = "Permissions"
			value = if (requiredPerms.isNotEmpty()) {
				"* ${
					"header.permissions.bot".translate(provider, null, bundle)
				}:${requiredPerms.formatPermissionsSet(null)}\n"
			} else {
				"None"
			}
		}
		if (extraDocs?.commandResult != null) {
			field {
				name = "Result"
				value =
					"* **${"header.result".translate(provider, null, bundle)}**:${
						extraDocs.commandResult!!.translate(provider, null, bundle)
					}\n"
			}
		}
	}

	private fun <T : ApplicationCommand<*>> createMessageUserCommandDocs(command: T, pagesObj: Pages) {
		val provider = command.translationsProvider
		val bundle = command.extension.bundle
		var extraDocs = command.additionalDocumentation[command.name]
		pagesObj.addPage(
			Page {
				title = command.name.translate(provider, null, bundle)
				createEmbed(null, command.requiredPerms, provider, bundle, extraDocs)
			}
		)
		extraDocs = null
	}
}
