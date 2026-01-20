/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.exceptions

/**
 * Exception thrown when an environment variable is not recognised.
 */
public class InvalidEnvironmentVariableException(variable: String, value: String) : Exception(
	"Unknown/unsupported value: $value for environment variable: $variable "
)
