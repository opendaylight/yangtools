/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Iterator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

class Bug5101Test extends AbstractYangTest {
    @Test
    void test() throws Exception {
        final ModuleEffectiveStatement module = assertEffectiveModel("/bugs/bug5101.yang")
            .getModuleStatement(QName.create("foo", "2016-01-29", "foo"));

        final ContainerEffectiveStatement myContainerInGrouping = module
            .findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow()
            .findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElse(null);
        assertThat(myContainerInGrouping, instanceOf(ContainerSchemaNode.class));
        assertEquals(Status.DEPRECATED, ((ContainerSchemaNode) myContainerInGrouping).getStatus());

        // This relies on schema definition order
        final Iterator<ContainerEffectiveStatement> containers =
            module.streamEffectiveSubstatements(ContainerEffectiveStatement.class)
                .collect(Collectors.toList()).iterator();

        final ContainerEffectiveStatement root = containers.next();
        assertThat(root, instanceOf(ContainerSchemaNode.class));
        assertEquals(Status.CURRENT, ((ContainerSchemaNode) root).getStatus());

        final ContainerEffectiveStatement rootMyContainer = root
            .streamEffectiveSubstatements(ContainerEffectiveStatement.class)
            .findAny().orElse(null);
        assertThat(rootMyContainer, instanceOf(ContainerSchemaNode.class));
        assertEquals(Status.DEPRECATED, ((ContainerSchemaNode) rootMyContainer).getStatus());

        final ContainerEffectiveStatement myContainer = containers.next();
        assertFalse(containers.hasNext());
        assertThat(myContainer, instanceOf(ContainerSchemaNode.class));
        assertEquals(Status.DEPRECATED, ((ContainerSchemaNode) myContainer).getStatus());

    }
}
