/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.enums

// TODO Support more file formats
/**
 * Enumeration containing the current supported file formats for documents.
 */
public enum class SupportedFileFormat(public val fileExtension: String) {
	MARKDOWN("md"),
}
