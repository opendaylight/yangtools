/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;

class YinFileStmtTest {
    @Test
    void readAndParseYinFileTestModel() throws Exception {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSource(createSource("test.yin"))
            .addSource(createSource("extension.yin"))
            .addSource(createSource("extension-use.yin"))
            .buildEffective());
    }

    // parsing yin file whose import statement references a module which does not exist
    @Test
    void readAndParseInvalidYinFileTest() {
        assertEquals("Imported module baar was not found [at <UNKNOWN>:8:27]",
            assertInstanceOf(InferenceException.class, assertThrows(SomeModifiersUnresolvedException.class,
                () -> RFC7950Reactors.defaultReactor().newBuild()
                    .addSource(createSource("incorrect-foo.yin"))
                    .buildEffective())
                .getCause())
            .getMessage());
    }

    // parsing yin file with duplicate key name in a list statement
    @Test
    void readAndParseInvalidYinFileTest2() {
        final var cause = assertInstanceOf(SourceException.class, assertThrows(SomeModifiersUnresolvedException.class,
            () -> RFC7950Reactors.defaultReactor().newBuild()
                .addSource(createSource("incorrect-bar.yin"))
                .buildEffective())
            .getCause());
        assertThat(cause.getMessage()).startsWith("Key argument 'testing-string testing-string' contains duplicates");
    }

    @NonNullByDefault
    private static YinDOMSource createSource(final String name) {
        return assertDoesNotThrow(() -> TestUtils.assertYinSource("/semantic-statement-parser/yin/" + name));
    }
}
