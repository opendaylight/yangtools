/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

public class Bug5942Test extends AbstractYangTest {
    @Test
    public void test() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug5942");

        final DataSchemaNode root = schemaContext.getDataChildByName(QName.create("foo", "2016-06-02", "root"));
        assertTrue(root instanceof ContainerSchemaNode);

        final var uses = ((ContainerSchemaNode) root).getUses();
        assertEquals(1, uses.size());
        final UsesNode usesNode = uses.iterator().next();

        assertEquals(Optional.of("uses description"), usesNode.getDescription());
        assertEquals(Optional.of("uses reference"), usesNode.getReference());
        assertEquals(Status.DEPRECATED, usesNode.getStatus());

        assertEquals("0!=1", usesNode.getWhenCondition().orElseThrow().toString());

        final UnrecognizedStatement unknownSchemaNode = usesNode.asEffectiveStatement().getDeclared()
            .findFirstDeclaredSubstatement(UnrecognizedStatement.class).orElseThrow();

        assertEquals("argument", unknownSchemaNode.argument());
        assertEquals(QName.create("foo", "2016-06-02", "e"),
            unknownSchemaNode.statementDefinition().getStatementName());
    }
}