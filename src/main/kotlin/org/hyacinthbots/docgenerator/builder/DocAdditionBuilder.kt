/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.builder

import org.hyacinthbots.docgenerator.annotations.DocAdditionBuilderDSL

/**
 * Builder class used for providing extra information for commands to be documented appropriately.
 */
@DocAdditionBuilderDSL
public open class DocAdditionBuilder {
	/**
	 * The result of the command.
	 *
	 * **Note**: This will not be displayed on commands that have sub-commands, even if it has data attached to it.
	 */
	public open var commandResult: String? = null

	/**
	 * Any additional information to attach alongside a document.
	 */
	public open var extraInformation: String? = null
}
