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
 * Enumeration for environment types.
 *
 * @property DEVELOPMENT A development environment. Docs will be generated
 * @property PRODUCTION A production environment. Docs will **not** be generated
 */
public enum class Environment(public val value: String) {
	DEVELOPMENT("development"),
	PRODUCTION("production")
}
