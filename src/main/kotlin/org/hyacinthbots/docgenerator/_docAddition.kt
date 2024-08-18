/*
 * Copyright (c) 2022-2024 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

import dev.kordex.core.commands.application.ApplicationCommand
import org.hyacinthbots.docgenerator.annotations.DocAdditionBuilderDSL
import org.hyacinthbots.docgenerator.builder.DocAdditionBuilder

/**
 * Provide additional documentation for commands.
 *
 * **Note**: This builder **will not** work in sub commands.
 *
 * @see DocAdditionBuilder
 */
@DocAdditionBuilderDSL
public suspend fun ApplicationCommand<*>.additionalDocumentation(
	builder: suspend DocAdditionBuilder.() -> Unit
) {
	val action = DocAdditionBuilder()
	action.builder()
	additionalDocumentation[this.name] = action
}

/**
 * Provide additional documentation for sub-commands.
 *
 * **Note**: This builder **will not** work in regular commands
 *
 * @see DocAdditionBuilder
 */
@DocAdditionBuilderDSL
public suspend fun ApplicationCommand<*>.subCommandAdditionalDocumentation(
	builder: suspend DocAdditionBuilder.() -> Unit
) {
	val action = DocAdditionBuilder()
	action.builder()
	subCommandAdditionalDocumentation[this.name] = action
}

/**
 * Internal variable to getting the additional documentation.
 */
@Suppress("UnusedReceiverParameter") // My goals are not known to you, mere compiler
internal var ApplicationCommand<*>.additionalDocumentation: MutableMap<String, DocAdditionBuilder?>
	get() = _additionalDocumentation
	set(value) {
		_additionalDocumentation = value
	}

/**
 * Internal variable to getting the additional documentation.
 */
@Suppress("UnusedReceiverParameter") // Just allow it my goodness
internal var ApplicationCommand<*>.subCommandAdditionalDocumentation: MutableMap<String, DocAdditionBuilder?>
	get() = _subCommandAdditionalDocumentation
	set(value) {
		_subCommandAdditionalDocumentation = value
	}

/**
 * Private variable to get the persistence of additional docs to work.
 */
@Suppress("ObjectPropertyName")
private var _additionalDocumentation: MutableMap<String, DocAdditionBuilder?> = mutableMapOf() // I hate but it works

/**
 * Private variable to get the persistence of additional docs to work.
 */
@Suppress("ObjectPropertyName")
private var _subCommandAdditionalDocumentation: MutableMap<String, DocAdditionBuilder?> = mutableMapOf() // More hatred
