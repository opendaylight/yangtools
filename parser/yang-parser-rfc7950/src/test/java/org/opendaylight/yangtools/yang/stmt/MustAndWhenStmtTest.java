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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

class MustAndWhenStmtTest {
    @Test
    void mustStmtTest() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/must-when-stmt-test/must-test.yang"))
            .buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModules("must-test").iterator().next();
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "interface"));
        assertNotNull(container);
        assertTrue(container.isPresenceContainer());

        final Collection<? extends MustDefinition> musts = container.getMustConstraints();
        assertEquals(2, musts.size());

        final Iterator<? extends MustDefinition> mustsIterator = musts.iterator();
        MustDefinition mustStmt = mustsIterator.next();
        assertThat(mustStmt.getXpath().toString(), anyOf(is("ifType != 'ethernet' or (ifType = 'ethernet' and "
            + "ifMTU = 1500)"), is("ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)")));
        assertThat(mustStmt.getErrorMessage(), anyOf(is(Optional.of("An ethernet MTU must be 1500")),
            is(Optional.of("An atm MTU must be 64 .. 17966"))));
        assertThat(mustStmt.getErrorAppTag(), anyOf(is(Optional.of("An ethernet error")),
            is(Optional.of("An atm error"))));
        mustStmt = mustsIterator.next();
        assertThat(mustStmt.getXpath().toString(), anyOf(
            is("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)"),
            is("ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)")));
        assertThat(mustStmt.getErrorMessage(), anyOf(is(Optional.of("An ethernet MTU must be 1500")),
            is(Optional.of("An atm MTU must be 64 .. 17966"))));
        assertThat(mustStmt.getErrorAppTag(), anyOf(is(Optional.of("An ethernet error")),
            is(Optional.of("An atm error"))));
    }

    @Test
    void whenStmtTest() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/must-when-stmt-test/when-test.yang"))
            .buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModules("when-test").iterator().next();
        assertNotNull(testModule);

        final ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "test-container"));
        assertNotNull(container);
        assertEquals("conditional-leaf = 'autumn-leaf'", container.getWhenCondition().orElseThrow().toString());
    }
}
