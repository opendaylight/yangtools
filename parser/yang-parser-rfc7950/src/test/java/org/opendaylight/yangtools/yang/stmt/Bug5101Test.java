/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

class Bug5101Test extends AbstractYangTest {
    @Test
    void test() throws Exception {
        final var module = assertEffectiveModel("/bugs/bug5101.yang")
            .getModuleStatement(QName.create("foo", "2016-01-29", "foo"));

        final var myContainerInGrouping = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(Status.DEPRECATED, assertInstanceOf(ContainerSchemaNode.class, myContainerInGrouping).getStatus());

        // This relies on schema definition order
        final Iterator<ContainerEffectiveStatement> containers =
            module.streamEffectiveSubstatements(ContainerEffectiveStatement.class).toList().iterator();

        final ContainerEffectiveStatement root = containers.next();
        assertEquals(Status.CURRENT, assertInstanceOf(ContainerSchemaNode.class, root).getStatus());

        final ContainerEffectiveStatement rootMyContainer = root
            .streamEffectiveSubstatements(ContainerEffectiveStatement.class)
            .findAny().orElse(null);
        assertEquals(Status.DEPRECATED, assertInstanceOf(ContainerSchemaNode.class, rootMyContainer).getStatus());

        final ContainerEffectiveStatement myContainer = containers.next();
        assertEquals(Status.DEPRECATED, assertInstanceOf(ContainerSchemaNode.class, myContainer).getStatus());
        assertFalse(containers.hasNext());
    }
}
