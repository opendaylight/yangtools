/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.ReactorDeclaredModel;

class AugmentArgumentParsingTest {

    private static final StatementStreamSource IMPORTED = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/imported.yang");
    private static final StatementStreamSource VALID_ARGS = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/root-valid-aug-args.yang");
    private static final StatementStreamSource INVALID_REL1 = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/root-invalid-rel1.yang");
    private static final StatementStreamSource INVALID_REL2 = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/root-invalid-rel2.yang");
    private static final StatementStreamSource INVALID_ABS = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/root-invalid-abs.yang");
    private static final StatementStreamSource INVALID_ABS_PREFIXED_NO_IMP = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/root-invalid-abs-no-imp.yang");
    private static final StatementStreamSource INVALID_EMPTY = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/root-invalid-empty.yang");
    private static final StatementStreamSource INVALID_XPATH = sourceForResource(
        "/semantic-statement-parser/augment-arg-parsing/root-invalid-xpath.yang");

    @Test
    void validAugAbsTest() throws ReactorException {
        final ReactorDeclaredModel result = RFC7950Reactors.defaultReactor().newBuild()
            .addSources(IMPORTED, VALID_ARGS)
            .build();
        assertNotNull(result);
    }

    @Test
    void invalidAugRel1Test() {
        assertSourceExceptionCause(assertReactorThrows(INVALID_REL1), "Augment argument './aug1/aug11' is not valid");
    }

    @Test
    void invalidAugRel2Test() {
        assertSourceExceptionCause(assertReactorThrows(INVALID_REL2), "Augment argument '../aug1/aug11' is not valid");
    }

    @Test
    void invalidAugAbs() {
        assertSourceExceptionCause(assertReactorThrows(INVALID_ABS),
            "'//aug1/aug11/aug111' is not a valid augment target-node on position 2: "
            + "'/' is not a valid prefix nor identifier [at ");
    }

    @Test
    void invalidAugAbsPrefixedNoImp() {
        assertSourceExceptionCause(assertReactorThrows(INVALID_ABS_PREFIXED_NO_IMP), "Failed to parse node 'imp:aug1'");
    }

    @Test
    void invalidAugEmptyTest() {
        final var ex = assertReactorThrows(INVALID_EMPTY);
        final var cause = assertInstanceOf(SourceException.class, ex.getCause());
        assertThat(cause.getMessage()).startsWith("Schema node identifier must not be empty");
    }

    @Test
    void invalidAugXPathTest() {
        final var ex = assertReactorThrows(INVALID_XPATH);
        final var cause = assertInstanceOf(SourceException.class, ex.getCause());
        assertThat(cause.getMessage()).startsWith("Failed to parse node '-' in path '/aug1/-'");

        final var nested = assertInstanceOf(SourceException.class, cause.getCause());
        assertThat(nested.getMessage()).startsWith("Invalid identifier '-'");
    }

    private static ReactorException assertReactorThrows(final StatementStreamSource source) {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild().addSources(source);
        return assertThrows(ReactorException.class, () -> reactor.build());
    }

    private static void assertSourceExceptionCause(final Throwable exception, final String start) {
        final var cause = assertInstanceOf(SourceException.class, exception.getCause());
        assertThat(cause.getMessage()).startsWith(start);
    }
}
