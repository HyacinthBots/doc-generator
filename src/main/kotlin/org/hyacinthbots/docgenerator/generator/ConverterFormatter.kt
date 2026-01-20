/*
 * Copyright (c) 2022-2026 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.i18n.Key
import docgenerator.i18n.Translations
import org.hyacinthbots.docgenerator.exceptions.InvalidConverterException
import java.util.Locale

/** The file path to converters. */
private const val STANDARD_PREFIX: String = "dev.kordex.core.commands.converters"

/** The file path to converters specifically for slash commands. */
private const val APPLICATION_PREFIX: String = "dev.kordex.core.commands.application.slash.converters"

/**
 * A class to facilitate the formatting of a converter from an unreadable file path to a readable, translatable string.
 *
 * @param converterString The string path of the converter
 * @param signatureType The converters signature
 */
internal class ConverterFormatter(
	private val converterString: String,
	private val signatureType: String,
) : KordExKoinComponent {

	/**
	 *  Strips the path to the convert file and remove the short commit hash from the end.
	 *  This allows the type to be read easily
	 */
	private val cleanConverterString: String =
		if (converterString.contains("commands.converters.impl.")) {
			converterString.removePrefix("$STANDARD_PREFIX.impl.").split("@")[0]
		} else if (converterString.contains("commands.application.slash")) {
			converterString.removePrefix("$APPLICATION_PREFIX.impl.").split("@")[0]
		} else {
			converterString.removePrefix("$STANDARD_PREFIX.").split("@")[0]
		}

	/**
	 * A map containing the converter types in code, formatted to a readable string.
	 */
	private val nonObviousConverterMap: Map<String, Key> = mapOf(
		"CoalescingToDefaultingConverter" to Translations.Signature.Type.Coalescing.defaulting,
		"CoalescingToOptionalConverter" to Translations.Signature.Type.Coalescing.optional,
		"SingleToDefaultingConverter" to Translations.Signature.Type.defaulting,
		"SingleToListConverter" to Translations.Signature.Type.list,
		"SingleToOptionalConverter" to Translations.Signature.Type.optional
	)

	/**
	 * A map of the converters, formatted to remove the `Converter` and reorder some words to make more sense.
	 */
	private val indicativeConverterMap: Map<String, Key> = mapOf(
		"AttachmentConverter" to Translations.Signature.attachment,
		"BooleanConverter" to Translations.Signature.boolean,
		"ChannelConverter" to Translations.Signature.channel,
		"ColorConverter" to Translations.Signature.color,
		"DecimalConverter" to Translations.Signature.decimal,
		"DurationConverter" to Translations.Signature.duration,
		"DurationCoalescingConverter" to Translations.Signature.durations,
		"EmailConverter" to Translations.Signature.email,
		"EmojiConverter" to Translations.Signature.emoji,
		"EnumConverter" to Translations.Signature.enum,
		"GuildConverter" to Translations.Signature.server,
		"IntConverter" to Translations.Signature.int,
		"LongConverter" to Translations.Signature.long,
		"MemberConverter" to Translations.Signature.member,
		"MessageConverter" to Translations.Signature.message,
		"RegexConverter" to Translations.Signature.regex,
		"RegexCoalescingConverter" to Translations.Signature.regexes,
		"RoleConverter" to Translations.Signature.role,
		"SnowflakeConverter" to Translations.Signature.snowflake,
		"StringCoalescingConverter" to Translations.Signature.strings,
		"StringConverter" to Translations.Signature.string,
		"SupportedLocaleConverter" to Translations.Signature.locale,
		"TimestampConverter" to Translations.Signature.timestamp,
		"UserConverter" to Translations.Signature.user,

		// Slash Converters

		"StringChoiceConverter" to Translations.Signature.stringchoice,
		"NumberChoiceConverter" to Translations.Signature.numberchoice,
		"EnumChoiceConverter" to Translations.Signature.enumchoice
	)

	/**
	 * The signature of the converter formatted into a readable and more formal string.
	 */
	private val signatureToType: Map<String, Key> = mapOf(
		"attachment" to Translations.Signature.attachment,
		"yes/no" to Translations.Signature.boolean,
		"channel" to Translations.Signature.channel,
		"colour" to Translations.Signature.color, // Yes both english and american-english spelling, why? Cringe.
		"color" to Translations.Signature.color,
		"decimal" to Translations.Signature.decimal,
		"duration" to Translations.Signature.duration,
		"email" to Translations.Signature.email,
		"server emoji" to Translations.Signature.emoji,
		"server" to Translations.Signature.server,
		"number" to Translations.Signature.number,
		"member" to Translations.Signature.member,
		"message" to Translations.Signature.message,
		"regex" to Translations.Signature.regex,
		"regexes" to Translations.Signature.regexes,
		"role" to Translations.Signature.role,
		"ID" to Translations.Signature.snowflake,
		"text" to Translations.Signature.string,
		"locale name/code" to Translations.Signature.locale,
		"timestamp" to Translations.Signature.timestamp,
		"user" to Translations.Signature.user
	)

	/**
	 * Formats the converter provided to a readable string, making use of the convert maps.
	 *
	 * @param language The [Locale] to translate the converters into
	 *
	 * @return The converter information formatted as a [String] and localised if necessary
	 */
	fun formatConverter(language: Locale = SupportedLocales.ENGLISH): String =
		if (converterString.contains(".impl.")) {
			// This converter is nice and easy to get as it specifies the type in the string.
			indicativeConverterMap[cleanConverterString]?.translateLocale(language)
				?: throw InvalidConverterException(cleanConverterString)
		} else {
			// This converter is not obvious, so must be built up from the non-obvious map and signature.
			"${nonObviousConverterMap[cleanConverterString]?.translateLocale(language)} ${
				signatureToType[signatureType]?.translateLocale(language) ?: signatureType
			}"
		}
}
