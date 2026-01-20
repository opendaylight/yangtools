/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataConstants;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

class YT1338Test extends AbstractYangDataTest {
    @Test
    void testAddedLeaves() throws Exception {
        final var restconf = assertInstanceOf(ContainerEffectiveStatement.class, newBuild()
            .addSource(sourceForYangText("""
                module yt1338 {
                  namespace yt1338;
                  prefix yt1338;

                  rpc foo;
                }"""))
            .buildEffective()
            .findModuleStatement(YangDataConstants.RFC8040_MODULE)
            .orElseThrow()
            .streamEffectiveSubstatements(YangDataEffectiveStatement.class)
            .filter(stmt -> YangDataStatementSupport.YANG_API.equals(stmt.argument()))
            .findAny()
            .orElseThrow()
            .findDataTreeNode(QName.create(YangDataConstants.RFC8040_MODULE, "restconf"))
            .orElseThrow());

        final var leaf = restconf.findDataTreeNode(QName.create(YangDataConstants.RFC8040_MODULE, "operations"))
            .orElseThrow()
            .findFirstEffectiveSubstatement(LeafEffectiveStatement.class)
            .orElseThrow();
        assertEquals(QName.create("yt1338", "foo"), leaf.argument());
        assertEquals(StatementOrigin.CONTEXT, leaf.statementOrigin());
        assertNull(leaf.declared());

        final var type = leaf.findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        assertEquals(TypeDefinitions.EMPTY, type.argument());
        assertEquals(StatementOrigin.CONTEXT, type.statementOrigin());
        assertNull(type.declared());
    }
}
