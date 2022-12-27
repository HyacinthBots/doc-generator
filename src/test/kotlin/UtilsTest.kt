/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

import com.kotlindiscord.kord.extensions.i18n.ResourceBundleTranslations
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import kotlinx.coroutines.runBlocking
import org.hyacinthbots.docgenerator.enums.ALL_COMMAND_TYPES
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.findOrCreateDocumentsFile
import org.hyacinthbots.docgenerator.formatPermissionsSet
import org.hyacinthbots.docgenerator.translate
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val path = Path("./build/testfile.md")
private val translationsProvider = ResourceBundleTranslations { defaultLocale }
private val defaultLocale = SupportedLocales.ENGLISH
private const val BUNDLE = "test"

class UtilsTest {
	@Test
	@Order(1)
	fun `test file creation`(): Unit = runBlocking {
		assertDoesNotThrow {
			findOrCreateDocumentsFile(path)
		}

		assertTrue(path.exists())
	}

	@Test
	@Order(2)
	fun `test translate`(): Unit = runBlocking {
		val english = "test.1".translate(translationsProvider, defaultLocale, BUNDLE)
		assertEquals("Good evening", english)

		val german = "test.1".translate(translationsProvider, SupportedLocales.GERMAN, BUNDLE)
		assertEquals("Guten Abend", german)
	}

	@Test
	@Order(3)
	fun `test permission formats`(): Unit = runBlocking {
		val set = mutableSetOf<Permission>(Permission.ModerateMembers)
		val formattedSet = set.formatPermissionsSet(null)
		assertEquals("Moderate Members", formattedSet)

		val permissions = Permissions(Permission.ModerateMembers)
		val formattedPerms = permissions.formatPermissionsSet(null)
		assertEquals("Moderate Members", formattedPerms)
	}

	@Test
	@Order(4)
	fun `ALL_COMMAND_TYPES contains all command types`(): Unit = runBlocking {
		CommandTypes.values().forEach {
			assertTrue("All commands types were not present in the list: $it is missing.") {
				it in ALL_COMMAND_TYPES
			}
		}
	}
}
