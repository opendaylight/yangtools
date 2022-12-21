/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
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
        BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild().addSource(KEY_COMP_DUPLICATE);
        try {
            reactor.build();
            fail("reactor.process should fail due to duplicate name in key");
        } catch (ReactorException e) {
            final Throwable cause = e.getCause();
            assertTrue(cause instanceof SourceException);
            assertTrue(cause.getMessage().startsWith("Key argument 'key1 key2 key2' contains duplicates"));
        }
    }
}
