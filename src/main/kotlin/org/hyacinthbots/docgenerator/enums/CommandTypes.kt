/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.enums

/**
 * The types of commands that can be documented.
 *
 * **Note**: Chat commands are not included as they are being phased out of Discord.
 *
 * @property SLASH Slash Commands
 * @property MESSAGE Message commands
 * @property USER User commands
 * @property ALL All command types as a list
 */
public enum class CommandTypes {
	SLASH,
	MESSAGE,
	USER;

	public companion object {
		public val ALL: List<CommandTypes> = listOf(SLASH, MESSAGE, USER)
	}
}
