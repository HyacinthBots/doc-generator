/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package testbot

import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralMessageCommand
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.ephemeralUserCommand
import dev.kordex.i18n.toKey

class TestExtension : Extension() {
	override val name: String
		get() = "test"

	override suspend fun setup() {
		ephemeralSlashCommand {
			name = "ping".toKey()
			description = "Play ping pong!".toKey()

			action {
				respond { content = "Pong!" }
			}
		}

		ephemeralUserCommand {
			name = "Ping".toKey()

			action {
				respond { content = event.interaction.user.mention }
			}
		}

		ephemeralMessageCommand {
			name = "Repeat".toKey()

			action {
				respond { content = event.interaction.target.fetchMessage().content }
			}
		}
	}
}
