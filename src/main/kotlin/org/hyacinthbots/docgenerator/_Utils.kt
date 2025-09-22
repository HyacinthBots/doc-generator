/*
 * Copyright (c) 2022-2024 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.utils.translate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hyacinthbots.docgenerator.generator.ConverterFormatter
import org.hyacinthbots.docgenerator.generator.DocsGenerator
import java.io.IOException
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.createFile
import kotlin.io.path.exists

/**
 * Searches for the target [path] and either finds it, creates it or complains.
 *
 * @param path The path to search
 */
internal suspend inline fun findOrCreateDocumentsFile(path: Path) {
	if (!path.exists()) {
		DocsGenerator.generatorLogger.debug { "File does not exist, creating..." }
		try {
			withContext(Dispatchers.IO) {
				path.createFile()
			}
		} catch (e: IOException) {
			DocsGenerator.generatorLogger.error(e) { e.message }
			return
		}
		DocsGenerator.generatorLogger.debug { "File created successfully..." }
	}
}

/**
 * Extension function for formatting a [MutableSet] of [Permission]s into a string, possibly localised if necessary.
 *
 * @param language The [Locale] to translate into
 *
 * @return A [String] list of the required permissions
 */
internal fun MutableSet<Permission>.formatPermissionsSet(language: Locale?): String {
	val permissionsSet: MutableSet<String> = mutableSetOf()
	this.forEach {
		permissionsSet.add(it.translate(language ?: SupportedLocales.ENGLISH))
	}

	return permissionsSet.toString().replace("[", "").replace("]", "")
}

/**
 * Extension function for formatting [Permissions] into a string, possibly localised if necessary.
 *
 * @param language The [Locale] to translate into
 *
 * @return A [String] list of the required permissions, or null if there are non
 */
internal fun Permissions?.formatPermissionsSet(language: Locale?): String? {
	this ?: return null
	val permissionsSet: MutableSet<String> = mutableSetOf()

	this.values.forEach { perm ->
		permissionsSet.add(
			Permission.entries.find { it.code.value == perm.code.value }
				?.translate(language ?: SupportedLocales.ENGLISH)
				?: Permission.fromShift(perm.shift).translate(language ?: SupportedLocales.ENGLISH)
		)
	}
	return permissionsSet.toString().replace("[", "").replace("]", "")
}

/**
 * function for formatting the arguments of a command into a nice string.
 *
 * @param arg The argument to translate
 * @param language the [Locale] to translate into
 *
 * @return The arguments, formatted into a presentable manner
 */
internal fun formatArguments(
	arg: Argument<*>,
	language: Locale = SupportedLocales.ENGLISH
): String =
	"\t* `${arg.displayName.translateLocale(language)}` - " +
		"${arg.description.translateLocale(language)} - " +
		"${
			ConverterFormatter(
				"${arg.converter}", arg.converter.signatureType.translateLocale(language)
			).formatConverter(language)
		}\n"

/**
 * Adds all a commands arguments to a string.
 *
 * @param command The command to get the args from
 * @param language The [Locale] to translate into
 *
 * @return A string containing all the commands arguments.
 */
internal fun addArguments(
	command: SlashCommand<*, *, *>,
	language: Locale = SupportedLocales.ENGLISH
): String {
	var argumentsString = ""
	command.arguments?.invoke()?.args?.forEach { arg ->
		argumentsString += formatArguments(arg, language)
	}

	return argumentsString
}

/** The name of the bundle containing this projects translations. */
internal const val DEFAULT_BUNDLE_NAME = "doc-generator"
