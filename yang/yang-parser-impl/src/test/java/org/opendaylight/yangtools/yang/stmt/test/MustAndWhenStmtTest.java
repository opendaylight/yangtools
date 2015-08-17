/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class MustAndWhenStmtTest {

    private static final YangStatementSourceImpl MUST_MODULE = new YangStatementSourceImpl
            ("/must-when-stmt-test/must-test.yang", false);
    private static final YangStatementSourceImpl WHEN_MODULE = new YangStatementSourceImpl("/must-when-stmt-test/" +
            "when-test.yang", false);

    @Test
    public void mustStmtTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, MUST_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("must-test", null);
        assertNotNull(testModule);

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName("interface");
        assertNotNull(container);
        assertTrue(container.isPresenceContainer());

        Set<MustDefinition> musts = container.getConstraints().getMustConstraints();
        assertEquals(2, musts.size());

        Iterator<MustDefinition> mustsIterator = musts.iterator();
        MustDefinition mustStmt = mustsIterator.next();
        assertThat(mustStmt.getXpath().toString(), anyOf(is("ifType != 'ethernet' or (ifType = 'ethernet' and " +
                "ifMTU = 1500)"), is("ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)")));
        assertThat(mustStmt.getErrorMessage(), anyOf(is("An ethernet MTU must be 1500"), is("An atm MTU must be 64 " +
                ".. 17966")));
        assertThat(mustStmt.getErrorAppTag(), anyOf(is("An ethernet error"), is("An atm error")));
        mustStmt = mustsIterator.next();
        assertThat(mustStmt.getXpath().toString(), anyOf(is("ifType != 'ethernet' or (ifType = 'ethernet' and " +
                "ifMTU = 1500)"), is("ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)")));
        assertThat(mustStmt.getErrorMessage(), anyOf(is("An ethernet MTU must be 1500"), is("An atm MTU must be 64 " +
                ".. 17966")));
        assertThat(mustStmt.getErrorAppTag(), anyOf(is("An ethernet error"), is("An atm error")));
    }

    @Test
    public void whenStmtTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, WHEN_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("when-test", null);
        assertNotNull(testModule);

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName("test-container");
        assertNotNull(container);
        assertEquals("conditional-leaf = 'autumn-leaf'", container.getConstraints().getWhenCondition().toString());
    }
}
