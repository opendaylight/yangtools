/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

class ImportResolutionBasicTest {

    private static final StatementStreamSource ROOT_WITHOUT_IMPORT = sourceForResource(
        "/semantic-statement-parser/import-arg-parsing/nature.yang");
    private static final StatementStreamSource IMPORT_ROOT = sourceForResource(
        "/semantic-statement-parser/import-arg-parsing/mammal.yang");
    private static final StatementStreamSource IMPORT_DERIVED = sourceForResource(
        "/semantic-statement-parser/import-arg-parsing/human.yang");
    private static final StatementStreamSource IMPORT_SELF = sourceForResource(
        "/semantic-statement-parser/import-arg-parsing/egocentric.yang");
    private static final StatementStreamSource CYCLE_YIN = sourceForResource(
        "/semantic-statement-parser/import-arg-parsing/cycle-yin.yang");
    private static final StatementStreamSource CYCLE_YANG = sourceForResource(
        "/semantic-statement-parser/import-arg-parsing/cycle-yang.yang");
    private static final StatementStreamSource FOO = sourceForResource(
        "/semantic-statement-parser/bug2649/foo.yang");
    private static final StatementStreamSource IMPORT = sourceForResource(
        "/semantic-statement-parser/bug2649/import-module.yang");


    @Test
    void inImportOrderTest() throws ReactorException {
        var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(ROOT_WITHOUT_IMPORT, IMPORT_ROOT, IMPORT_DERIVED)
            .build();
        assertNotNull(result);
    }

    @Test
    void inInverseOfImportOrderTest() throws ReactorException {
        var result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(IMPORT_DERIVED, IMPORT_ROOT, ROOT_WITHOUT_IMPORT)
            .build();
        assertNotNull(result);
    }

    @Test
    void missingImportedSourceTest() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(IMPORT_DERIVED, ROOT_WITHOUT_IMPORT);
        assertFailedPreLinkage(reactor::build, "mammal");
    }

    @Test
    void circularImportsTest() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(CYCLE_YIN, CYCLE_YANG);
        assertFailedPreLinkage(reactor::build, "cycle-");
    }

    @Test
    void selfImportTest() {
        var reactor = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(IMPORT_SELF, IMPORT_ROOT, ROOT_WITHOUT_IMPORT);
        assertFailedPreLinkage(reactor::build, "egocentric");
    }

    @Test
    void bug2649Test() throws ReactorException {
        var buildEffective = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(FOO, IMPORT)
            .buildEffective();
        assertNotNull(buildEffective);
    }

    private static void assertFailedPreLinkage(final Callable<?> callable, final String name) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class, callable::call);
        assertEquals(ModelProcessingPhase.SOURCE_PRE_LINKAGE, ex.getPhase());
        assertEquals("Some of SOURCE_PRE_LINKAGE modifiers for statements were not resolved.", ex.getMessage());
        final var cause = assertInstanceOf(InferenceException.class, ex.getCause());
        assertThat(cause.getMessage(), allOf(
            startsWith("Imported module [" + name),
            containsString("] was not found. [at ")));
    }
}
