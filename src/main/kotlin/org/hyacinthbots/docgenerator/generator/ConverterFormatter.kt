/*
 * Copyright (c) 2022-2024 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import dev.kordex.core.i18n.ResourceBundleTranslations
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.koin.KordExKoinComponent
import org.hyacinthbots.docgenerator.DEFAULT_BUNDLE_NAME
import org.hyacinthbots.docgenerator.exceptions.InvalidConverterException
import org.hyacinthbots.docgenerator.translate
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
 * @param language The language to translate into, defaults to english
 */
internal class ConverterFormatter(
	private val converterString: String,
	private val signatureType: String,
	private val language: Locale = SupportedLocales.ENGLISH
) : KordExKoinComponent {
	/**
	 * The language for the translation.
	 */
	private val translationsProvider: TranslationsProvider = ResourceBundleTranslations { language }

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
	private val nonObviousConverterMap: Map<String, String> = mapOf(
		"CoalescingToDefaultingConverter" to "Coalescing Defaulting",
		"CoalescingToOptionalConverter" to "Coalescing Optional",
		"SingleToDefaultingConverter" to "Defaulting",
		"SingleToListConverter" to "List",
		"SingleToOptionalConverter" to "Optional"
	)

	/**
	 * A map of the converters, formatted to remove the `Converter` and reorder some words to make more sense.
	 */
	private val indicativeConverterMap: Map<String, String> = mapOf(
		"AttachmentConverter" to "Attachment",
		"BooleanConverter" to "Boolean",
		"ChannelConverter" to "Channel",
		"ColorConverter" to "Color",
		"DecimalConverter" to "Decimal",
		"DurationConverter" to "Duration",
		"DurationCoalescingConverter" to "Coalescing Duration",
		"EmailConverter" to "Email",
		"EmojiConverter" to "Emoji",
		"EnumConverter" to "Enum",
		"GuildConverter" to "Guild",
		"IntConverter" to "Int",
		"LongConverter" to "Long",
		"MemberConverter" to "Member",
		"MessageConverter" to "Message",
		"RegexConverter" to "Regex",
		"RegexCoalescingConverter" to "Coalescing Regex",
		"RoleConverter" to "Role",
		"SnowflakeConverter" to "Snowflake",
		"StringCoalescingConverter" to "Coalescing String",
		"StringConverter" to "String",
		"SupportedLocaleConverter" to "Supported Locale",
		"TimestampConverter" to "Timestamp",
		"UserConverter" to "User",

		// Slash Converters

		"StringChoiceConverter" to "String Choice",
		"NumberChoiceConverter" to "Number Choice",
		"EnumChoiceConverter" to "Enum Choice"
	)

	/**
	 * The signature of the converter formatted into a readable and more formal string.
	 */
	private val signatureToType: Map<String, String> = mapOf(
		"attachment" to translationsProvider.translate("signature.attachment", bundleName = DEFAULT_BUNDLE_NAME),
		"yes/no" to translationsProvider.translate("signature.boolean", bundleName = DEFAULT_BUNDLE_NAME),
		"channel" to translationsProvider.translate("signature.channel", bundleName = DEFAULT_BUNDLE_NAME),
		"color" to translationsProvider.translate("signature.color", bundleName = DEFAULT_BUNDLE_NAME),
		"decimal" to translationsProvider.translate("signature.decimal", bundleName = DEFAULT_BUNDLE_NAME),
		"duration" to translationsProvider.translate("signature.duration", bundleName = DEFAULT_BUNDLE_NAME),
		"email" to translationsProvider.translate("signature.email", bundleName = DEFAULT_BUNDLE_NAME),
		"server emoji" to translationsProvider.translate("signature.emoji", bundleName = DEFAULT_BUNDLE_NAME),
		"server" to translationsProvider.translate("signature.server", bundleName = DEFAULT_BUNDLE_NAME),
		"number" to translationsProvider.translate("signature.number", bundleName = DEFAULT_BUNDLE_NAME),
		"member" to translationsProvider.translate("signature.member", bundleName = DEFAULT_BUNDLE_NAME),
		"message" to translationsProvider.translate("signature.message", bundleName = DEFAULT_BUNDLE_NAME),
		"regex" to translationsProvider.translate("signature.regex", bundleName = DEFAULT_BUNDLE_NAME),
		"regexes" to translationsProvider.translate("signature.regexes", bundleName = DEFAULT_BUNDLE_NAME),
		"role" to translationsProvider.translate("signature.role", bundleName = DEFAULT_BUNDLE_NAME),
		"ID" to translationsProvider.translate("signature.snowflake", bundleName = DEFAULT_BUNDLE_NAME),
		"text" to translationsProvider.translate("signature.string", bundleName = DEFAULT_BUNDLE_NAME),
		"locale name/code" to translationsProvider.translate("signature.locale", bundleName = DEFAULT_BUNDLE_NAME),
		"timestamp" to translationsProvider.translate("signature.timestamp", bundleName = DEFAULT_BUNDLE_NAME),
		"user" to translationsProvider.translate("signature.user", bundleName = DEFAULT_BUNDLE_NAME)
	)

	/**
	 * Formats the converter provided to a readable string, making use of the convert maps.
	 *
	 * @param language The [Locale] to translate the converters into
	 *
	 * @return The converter information formatted as a [String] and localised if necessary
	 */
	fun formatConverter(language: Locale? = null): String {
		val translatedSignatureType = translationsProvider.translate(signatureType, SupportedLocales.ENGLISH)

		return if (converterString.contains(".impl.")) {
			// This converter is nice and easy to get as it specifies the type in the string.
			indicativeConverterMap[cleanConverterString] ?: throw InvalidConverterException(cleanConverterString)
		} else {
			// This converter is not obvious, so must be built up from the non-obvious map and signature.
			"${nonObviousConverterMap[cleanConverterString]} ${
				(signatureToType[translatedSignatureType] ?: signatureType).translate(
					translationsProvider, language, DEFAULT_BUNDLE_NAME
				)
			}"
		}
	}
}
