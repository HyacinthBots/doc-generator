/*
 * Copyright (c) 2022-2024 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

import dev.kordex.core.ExtensibleBot
import kotlinx.coroutines.runBlocking
import org.hyacinthbots.docgenerator.exceptions.InvalidConverterException
import org.hyacinthbots.docgenerator.generator.ConverterFormatter
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

private val token = System.getenv("TEST_TOKEN")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConverterTest {

	@BeforeAll
	fun startBot(): Unit = runBlocking {
		ExtensibleBot(token) {}
	}

	@Test
	@Order(1)
	fun `test converter`(): Unit = runBlocking {
		var converter1: String? = null
		var converter2: String? = null
		var converter3: String? = null
		assertThrows<InvalidConverterException> {
			ConverterFormatter(
				"dev.kordex.core.commands.converters.impl.NotRealConverter",
				"fake"
			).formatConverter()
		}

		assertDoesNotThrow {
			converter1 = ConverterFormatter(
				"dev.kordex.core.commands.converters.impl.ChannelConverter",
				"channel"
			).formatConverter()

			converter2 = ConverterFormatter(
				"dev.kordex.core.commands.application.slash.converters.impl.StringChoiceConverter",
				""
			).formatConverter()

			converter3 = ConverterFormatter(
				"dev.kordex.core.commands.converters.SingleToDefaultingConverter",
				"channel"
			).formatConverter()
		}

		assertEquals("Channel", converter1)
		assertEquals("String Choice", converter2)
		assertEquals("Defaulting Channel", converter3)
	}
}
