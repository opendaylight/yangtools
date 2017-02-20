/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class KeyTest {

    private static final StatementStreamSource KEY_SIMPLE_AND_COMP = sourceForResource(
            "/semantic-statement-parser/key-arg-parsing/key-simple-and-comp.yang");
    private static final StatementStreamSource KEY_COMP_DUPLICATE = sourceForResource(
            "/semantic-statement-parser/key-arg-parsing/key-comp-duplicate.yang");

    @Test
    public void keySimpleTest() throws ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(KEY_SIMPLE_AND_COMP);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void keyCompositeInvalid() {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(KEY_COMP_DUPLICATE);

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
