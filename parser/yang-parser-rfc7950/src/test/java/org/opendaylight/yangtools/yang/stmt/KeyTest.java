/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
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

class KeyTest {

    private static final StatementStreamSource KEY_SIMPLE_AND_COMP = sourceForResource(
        "/semantic-statement-parser/key-arg-parsing/key-simple-and-comp.yang");
    private static final StatementStreamSource KEY_COMP_DUPLICATE = sourceForResource(
        "/semantic-statement-parser/key-arg-parsing/key-comp-duplicate.yang");

    @Test
    void keySimpleTest() throws ReactorException {
        ReactorDeclaredModel result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(KEY_SIMPLE_AND_COMP)
            .build();
        assertNotNull(result);
    }

    @Test
    void keyCompositeInvalid() {
        final var reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(KEY_COMP_DUPLICATE);
        final var cause = assertThrows(ReactorException.class, reactor::build).getCause();
        assertInstanceOf(SourceException.class, cause);
        assertThat(cause.getMessage(), startsWith("Key argument 'key1 key2 key2' contains duplicates"));
    }
}
