/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.spi.source.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.xml.sax.SAXException;

class YinFileStmtTest {

    private static final StatementStreamSource YIN_FILE = createSource("test.yin");
    private static final StatementStreamSource EXT_FILE = createSource("extension.yin");
    private static final StatementStreamSource EXT_USE_FILE = createSource("extension-use.yin");
    private static final StatementStreamSource INVALID_YIN_FILE = createSource("incorrect-foo.yin");
    private static final StatementStreamSource INVALID_YIN_FILE_2 = createSource("incorrect-bar.yin");

    private static StatementStreamSource createSource(final String name) {
        try {
            return YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                YinTextSchemaSource.forResource(YinFileStmtTest.class, "/semantic-statement-parser/yin/" + name)));
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test
    void readAndParseYinFileTestModel() throws ReactorException {
        assertNotNull(RFC7950Reactors.defaultReactor().newBuild()
            .addSources(YIN_FILE, EXT_FILE, EXT_USE_FILE)
            .buildEffective());
    }

    // parsing yin file whose import statement references a module which does not exist
    @Test
    void readAndParseInvalidYinFileTest() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(INVALID_YIN_FILE);
        assertThrows(SomeModifiersUnresolvedException.class, reactor::buildEffective);
    }

    // parsing yin file with duplicate key name in a list statement
    @Test
    void readAndParseInvalidYinFileTest2() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(INVALID_YIN_FILE_2);
        final var cause = assertThrows(SomeModifiersUnresolvedException.class, reactor::buildEffective).getCause();
        assertInstanceOf(SourceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Key argument 'testing-string testing-string' contains duplicates"));
    }
}