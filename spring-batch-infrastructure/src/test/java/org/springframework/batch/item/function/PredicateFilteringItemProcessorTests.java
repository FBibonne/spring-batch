/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.item.function;

import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link PredicateFilteringItemProcessor}.
 *
 * @author Mahmoud Ben Hassine
 */
class PredicateFilteringItemProcessorTests {

	private final Predicate<String> foos = item -> item.startsWith("foo");

	@Test
	void testMandatoryPredicate() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new PredicateFilteringItemProcessor<String>(null),
				"A predicate is required");
	}

	@Test
	void testProcess() throws Exception {
		// given
		PredicateFilteringItemProcessor<String> processor = new PredicateFilteringItemProcessor<>(this.foos);

		// when & then
		Assertions.assertNull(processor.process("foo1"));
		Assertions.assertNull(processor.process("foo2"));
		Assertions.assertEquals("bar", processor.process("bar"));
	}

}