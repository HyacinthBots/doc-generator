/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package testbot

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.utils.env
import org.hyacinthbots.docgenerator.docsGenerator
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import kotlin.io.path.Path

suspend fun main() {
	val bot = ExtensibleBot(env("TOKEN")) {
		extensions {
			add(::TestExtension)
		}

		docsGenerator {
			enabled = true
			fileFormat = SupportedFileFormat.MARKDOWN
			commandTypes = CommandTypes.ALL
			filePath = Path("commands.md")
			environment = "development"
			useBuiltinCommandList = true
			botName = "Test"
		}
	}

	bot.start()
}
