/*
 * Copyright (c) 2019 Ericsson AB. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;

class YT971Test extends AbstractYangTest {
    private static final QNameModule NAMESPACE = QNameModule.of("test", "2019-03-25");

    @Test
    void testEscapeLexer() {
        final var context = assertEffectiveModel("/bugs/YT971/test.yang");

        final DataSchemaNode someContainer = context.getDataChildByName(
            QName.create(NAMESPACE, "some-container"));
        final ContainerSchemaNode containerSchemaNode = assertInstanceOf(ContainerSchemaNode.class, someContainer);
        final DataSchemaNode someLeaf = containerSchemaNode.getDataChildByName(QName.create(NAMESPACE, "some-leaf"));
        final LeafSchemaNode leafSchemaNode = assertInstanceOf(LeafSchemaNode.class, someLeaf);
        assertEquals(Optional.of("Some string that ends with a backslash (with escape backslash too) \\"),
            leafSchemaNode.getDescription());
        assertInstanceOf(Int16TypeDefinition.class, leafSchemaNode.getType());

        final DataSchemaNode someOtherLeaf = containerSchemaNode.getDataChildByName(
            QName.create(NAMESPACE, "some-other-leaf"));

        final LeafSchemaNode otherLeafSchemaNode = assertInstanceOf(LeafSchemaNode.class, someOtherLeaf);
        assertEquals(Optional.of("Some string after the double backslash"), otherLeafSchemaNode.getDescription());
        assertInstanceOf(Int32TypeDefinition.class, otherLeafSchemaNode.getType());
    }
}
