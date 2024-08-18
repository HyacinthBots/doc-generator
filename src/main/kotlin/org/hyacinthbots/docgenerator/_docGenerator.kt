/*
 * Copyright (c) 2022-2024 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

import dev.kordex.core.builders.ExtensibleBotBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.hyacinthbots.docgenerator.annotations.ConfigurationBuilderDSL
import org.hyacinthbots.docgenerator.builder.ConfigurationBuilder
import org.hyacinthbots.docgenerator.enums.Environment
import org.hyacinthbots.docgenerator.exceptions.InvalidEnvironmentVariableException
import org.hyacinthbots.docgenerator.extensions.CommandList
import org.hyacinthbots.docgenerator.generator.DocsGenerator

private val generatorLogger = KotlinLogging.logger {}

/**
 * DSL for configuring the options of doc-generator.
 *
 * @see ConfigurationBuilder
 */
@ConfigurationBuilderDSL
public suspend fun ExtensibleBotBuilder.docsGenerator(
	builder: suspend ConfigurationBuilder.() -> Unit
) {
	val action = ConfigurationBuilder()
	action.builder()

	if (action.useBuiltinCommandList) {
		extensions {
			add {
				CommandList(action.botName, action.commandTypes)
			}
		}
	}

	hooks {
		afterExtensionsAdded {
			if (!action.enabled) {
				generatorLogger.debug { "Doc generation disabled, not generating!" }
				return@afterExtensionsAdded
			}

			when (action.environment.lowercase()) {
				Environment.PRODUCTION.value -> {
					generatorLogger.debug { "Production environment detected, not generating!" }
					return@afterExtensionsAdded
				}

				Environment.DEVELOPMENT.value -> {
					if (action.commandTypes.isEmpty()) {
						generatorLogger.error { "No command types have been specified! Please specify command types to document" }
						return@afterExtensionsAdded
					}

					DocsGenerator.updateDocumentsFile(
						action.filePath,
						action.fileFormat,
						action.commandTypes,
						extensions.values.toMutableList(),
						if (action.translationSupport.enableTranslations) action.translationSupport.supportedLanguages else null
					)
				}

				else -> throw InvalidEnvironmentVariableException("environment", action.environment)
			}
		}
	}
}
