/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class Bug7879Test extends AbstractYangTest {
    private static final XMLNamespace NS = XMLNamespace.of("my-model-ns");

    @Test
    void test() throws Exception {
        final var module = assertEffectiveModelDir("/bugs/bug7879")
            .getModuleStatement(qn("my-model"));

        assertInstanceOf(ContainerSchemaNode.class, module.findSchemaTreeNode(
            qn("my-alarm"), qn("my-content"), qn("my-event-container")).orElseThrow());

        assertEquals(Optional.of("new description"),
            assertInstanceOf(LeafSchemaNode.class, module.findSchemaTreeNode(
                qn("my-alarm"), qn("my-content"), qn("my-event-value")).orElseThrow()).getDescription());
    }

    private static QName qn(final String localName) {
        return QName.create(NS, localName);
    }
}
