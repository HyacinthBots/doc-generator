/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.builder

import org.hyacinthbots.docgenerator.annotations.ConfigurationBuilderDSL
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import java.nio.file.Path

/**
 * Builder class used for configuring the documentation options for the bot.
 */
@ConfigurationBuilderDSL
public open class ConfigurationBuilder {
	/** Whether to enable the documentation generator or not. */
	public open var enabled: Boolean = false

	/**
	 *  The file format to write the documents into.
	 *
	 *  @see SupportedFileFormat
	 */
	public open lateinit var fileFormat: SupportedFileFormat

	/**
	 * The list of command types to document.
	 *
	 * @see CommandTypes
	 */
	public open lateinit var commandTypes: List<CommandTypes>

	/**
	 *  The path to the file. Must include the file name and extension.
	 *
	 *  **Note:** If translation support is enabled, the language tag will be inserted before the file extension on
	 *  each localised file.
	 */
	public open lateinit var filePath: Path

	/**
	 * The environment the bot is being run in. `Development` or `Production`, ideally provided as an env var.
	 *
	 * When this value is `production`, the doc generation will not run, to not fill up the production environment with
	 * needless files.
	 */
	public open lateinit var environment: String

	/**
	 * Whether to use the provided slash command containing a list of commands this bot uses.
	 */
	public open var useBuiltinCommandList: Boolean = false

	/**
	 * The name of the bot the documentation is being generated for. Leave blank to pull it automatically.
	 */
	public open var botName: String? = null

	/** @suppress Builder that should not be directly set by the user. */
	public val translationSupport: TranslationSupportBuilder = TranslationSupportBuilder()

	/**
	 * DSL function used to configure the documentation translation options.
	 *
	 * @see TranslationSupportBuilder
	 */
	@ConfigurationBuilderDSL
	public fun translationSupport(builder: TranslationSupportBuilder.() -> Unit) {
		builder(translationSupport)
	}
}
