/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.exceptions

import org.hyacinthbots.docgenerator.builder.ConfigurationBuilder

/**
 * Exception thrown when the file format in the [ConfigurationBuilder.fileFormat] differs from the one specified in
 * the [ConfigurationBuilder.filePath].
 */
public class ConflictingFileFormatException(expected: String, actual: String) : Exception(
	"Conflicting file formats detected. Expected $expected but got $actual"
)
