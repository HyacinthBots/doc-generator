/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.extensions

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.commands.application.ApplicationCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.pagination.PublicResponsePaginator
import dev.kordex.core.pagination.pages.Page
import dev.kordex.core.pagination.pages.Pages
import docgenerator.i18n.Translations
import org.hyacinthbots.docgenerator.DEFAULT_BUNDLE_NAME
import org.hyacinthbots.docgenerator.addArguments
import org.hyacinthbots.docgenerator.additionalDocumentation
import org.hyacinthbots.docgenerator.builder.DocAdditionBuilder
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.externalBundle
import org.hyacinthbots.docgenerator.formatPermissionsSet
import org.hyacinthbots.docgenerator.subCommandAdditionalDocumentation

public class CommandList(private val botName: String?, private val enabledCommands: List<CommandTypes>) : Extension() {
	override val name: String = "command-list"

	override suspend fun setup() {
		val pagesObj = Pages()

		bot.extensions.values.toMutableList().forEach {
			if (enabledCommands.contains(CommandTypes.SLASH)) {
				it.slashCommands.forEach { slashCommand ->
					if (slashCommand.subCommands.isNotEmpty()) {
						slashCommand.subCommands.forEach { subCommand ->
							var arguments: String? = addArguments(subCommand)
							if (arguments?.isEmpty() == true) arguments = null
							var subExtraDocs = subCommand.subCommandAdditionalDocumentation[subCommand.name.translate()]
							pagesObj.addPage(
								Page {
									title = "${subCommand.parentCommand?.name?.translate()}" +
										" ${subCommand.name.translate()}"
									description = subCommand.description.translate()
									createEmbed(arguments, subCommand.requiredPerms, externalBundle, subExtraDocs, slashCommand.type)
								}
							)
							subExtraDocs = null
						}
					} else {
						var arguments: String?
						arguments = addArguments(slashCommand)
						if (arguments.isEmpty()) arguments = null
						var extraDocs = slashCommand.additionalDocumentation[slashCommand.name.translate()]
						pagesObj.addPage(
							Page {
								title = slashCommand.name.translate()
								description = slashCommand.description.translate()
								createEmbed(arguments, slashCommand.requiredPerms, externalBundle, extraDocs, slashCommand.type)
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
			name = Translations.Commandlist.name
			description =
				Translations.Commandlist.description.withOrdinalPlaceholders(botName ?: kord.getSelf().username)

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
	 * @param bundle The bundle to get the translations from
	 * @param extraDocs Any extra documentation for the command
	 */
	private fun EmbedBuilder.createEmbed(
		args: String?,
		requiredPerms: MutableSet<Permission>,
		bundle: String?,
		extraDocs: DocAdditionBuilder?,
		commandType: ApplicationCommandType
	) {
		field {
			name = "Arguments"
			value = args ?: "None"
		}
		field {
			name = "Permissions"
			value = if (requiredPerms.isNotEmpty()) {
				"* ${
					Translations.Header.Permissions.bot.translate()
				}:${requiredPerms.formatPermissionsSet(null)}\n"
			} else {
				"None"
			}
		}
		if (extraDocs?.commandResult != null) {
			val resultAsKey = extraDocs.commandResult!!.toKey(bundle ?: DEFAULT_BUNDLE_NAME)
			field {
				name = "Result"
				value =
					"* **${Translations.Header.result}**:${resultAsKey.translate()}\n"
			}
		}
		footer {
			text = "Command type: ${convertType(commandType)}"
		}
	}

	private fun <T : ApplicationCommand<*>> createMessageUserCommandDocs(command: T, pagesObj: Pages) {
		var extraDocs = command.additionalDocumentation[command.name.translate()]
		pagesObj.addPage(
			Page {
				title = command.name.translate()
				createEmbed(null, command.requiredPerms, externalBundle, extraDocs, command.type)
			}
		)
		extraDocs = null
	}

	private fun convertType(type: ApplicationCommandType): String =
		when (type.value) {
			1 -> "Slash"
			2 -> "User"
			3 -> "Message"
			else -> "Unknown"
		}
}
