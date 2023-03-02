/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataConstants;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class YT1338Test extends AbstractYangDataTest {
    @Test
    public void testAddedLeaves() throws ReactorException {
        final var restconf = REACTOR.newBuild().addSources(IETF_RESTCONF_MODULE, sourceForResource("/yt1338/foo.yang"))
            .buildEffective()
            .findModuleStatement(YangDataConstants.RFC8040_MODULE)
            .orElseThrow()
            .streamEffectiveSubstatements(YangDataEffectiveStatement.class)
            .filter(stmt -> YangDataStatementSupport.YANG_API.equals(stmt.argument()))
            .findAny()
            .orElseThrow()
            .findDataTreeNode(QName.create(YangDataConstants.RFC8040_MODULE, "restconf"))
            .orElseThrow();
        assertThat(restconf, instanceOf(ContainerEffectiveStatement.class));

        final var leaf = ((ContainerEffectiveStatement) restconf)
            .findDataTreeNode(QName.create(YangDataConstants.RFC8040_MODULE, "operations"))
            .orElseThrow()
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class)
            .orElseThrow();
        assertEquals(QName.create("foo", "foo"), leaf.argument());
        assertEquals(StatementOrigin.CONTEXT, leaf.statementOrigin());
        assertNull(leaf.getDeclared());

        final var type = leaf.findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        assertEquals(TypeDefinitions.EMPTY, type.argument());
        assertEquals(StatementOrigin.CONTEXT, type.statementOrigin());
        assertNull(type.getDeclared());
    }
}
