/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

public class KeyTest {

    private static final YangStatementSourceImpl KEY_SIMPLE_AND_COMP = new YangStatementSourceImpl(
            "/semantic-statement-parser/key-arg-parsing/key-simple-and-comp.yang");
    private static final YangStatementSourceImpl KEY_COMP_DUPLICATE = new YangStatementSourceImpl(
            "/semantic-statement-parser/key-arg-parsing/key-comp-duplicate.yang");

    @Test
    public void keySimpleTest() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, KEY_SIMPLE_AND_COMP);

        EffectiveModelContext result = reactor.build();
        assertNotNull(result);
    }

    @Test
    public void keyCompositeInvalid() throws SourceException, ReactorException {

        BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, KEY_COMP_DUPLICATE);

        try {
            reactor.build();
            fail("reactor.process should fail due to duplicate name in key");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    private static void addSources(final BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }

}
