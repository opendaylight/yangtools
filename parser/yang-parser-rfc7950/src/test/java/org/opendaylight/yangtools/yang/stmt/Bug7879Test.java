/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

public class Bug7879Test extends AbstractYangTest {
    private static final XMLNamespace NS = XMLNamespace.of("my-model-ns");

    @Test
    public void test() throws Exception {
        final ModuleEffectiveStatement module = assertEffectiveModelDir("/bugs/bug7879")
            .getModuleStatement(qn("my-model"));

        final SchemaTreeEffectiveStatement<?> container = module.findSchemaTreeNode(
            qn("my-alarm"), qn("my-content"), qn("my-event-container")).orElse(null);
        assertThat(container, instanceOf(ContainerSchemaNode.class));

        final SchemaTreeEffectiveStatement<?> leaf = module.findSchemaTreeNode(
            qn("my-alarm"), qn("my-content"), qn("my-event-value")).orElse(null);
        assertThat(leaf, instanceOf(LeafSchemaNode.class));
        assertEquals(Optional.of("new description"), ((LeafSchemaNode) leaf).getDescription());
    }

    private static QName qn(final String localName) {
        return QName.create(NS, localName);
    }
}
