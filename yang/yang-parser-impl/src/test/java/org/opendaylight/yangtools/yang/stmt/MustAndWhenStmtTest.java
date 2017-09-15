/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class MustAndWhenStmtTest {

    private static final StatementStreamSource MUST_MODULE = sourceForResource("/must-when-stmt-test/must-test.yang");
    private static final StatementStreamSource WHEN_MODULE = sourceForResource("/must-when-stmt-test/when-test.yang");

    @Test
    public void mustStmtTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(MUST_MODULE);

        final SchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("must-test", null);
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "interface"));
        assertNotNull(container);
        assertTrue(container.isPresenceContainer());

        final Set<MustDefinition> musts = container.getConstraints().getMustConstraints();
        assertEquals(2, musts.size());

        final Iterator<MustDefinition> mustsIterator = musts.iterator();
        MustDefinition mustStmt = mustsIterator.next();
        assertThat(mustStmt.getXpath().toString(), anyOf(is("ifType != 'ethernet' or (ifType = 'ethernet' and "
                + "ifMTU = 1500)"), is("ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)")));
        assertThat(mustStmt.getErrorMessage(), anyOf(is("An ethernet MTU must be 1500"), is("An atm MTU must be 64 "
                + ".. 17966")));
        assertThat(mustStmt.getErrorAppTag(), anyOf(is("An ethernet error"), is("An atm error")));
        mustStmt = mustsIterator.next();
        assertThat(mustStmt.getXpath().toString(), anyOf(is("ifType != 'ethernet' or (ifType = 'ethernet' and "
                + "ifMTU = 1500)"), is("ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)")));
        assertThat(mustStmt.getErrorMessage(), anyOf(is("An ethernet MTU must be 1500"), is("An atm MTU must be 64 "
                + ".. 17966")));
        assertThat(mustStmt.getErrorAppTag(), anyOf(is("An ethernet error"), is("An atm error")));
    }

    @Test
    public void whenStmtTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(WHEN_MODULE);

        final SchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("when-test", null);
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container"));
        assertNotNull(container);
        assertEquals("conditional-leaf = 'autumn-leaf'", container.getConstraints().getWhenCondition().toString());
    }
}
