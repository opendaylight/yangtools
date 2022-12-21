/*
 * Copyright (c) 2019 Ericsson AB. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;

class YT971Test extends AbstractYangTest {
    private static final QNameModule NAMESPACE = QNameModule.create(XMLNamespace.of("test"), Revision.of("2019-03-25"));

    @Test
    void testEscapeLexer() {
        final var context = assertEffectiveModel("/bugs/YT971/test.yang");

        final DataSchemaNode someContainer = context.getDataChildByName(
            QName.create(NAMESPACE, "some-container"));
        assertThat(someContainer, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) someContainer;

        final DataSchemaNode someLeaf = containerSchemaNode.getDataChildByName(QName.create(NAMESPACE, "some-leaf"));
        assertThat(someLeaf, instanceOf(LeafSchemaNode.class));
        final LeafSchemaNode leafSchemaNode = (LeafSchemaNode) someLeaf;
        assertEquals(Optional.of("Some string that ends with a backslash (with escape backslash too) \\"),
            leafSchemaNode.getDescription());
        assertThat(leafSchemaNode.getType(), instanceOf(Int16TypeDefinition.class));

        final DataSchemaNode someOtherLeaf = containerSchemaNode.getDataChildByName(
            QName.create(NAMESPACE, "some-other-leaf"));
        assertThat(someOtherLeaf, instanceOf(LeafSchemaNode.class));

        final LeafSchemaNode otherLeafSchemaNode = (LeafSchemaNode) someOtherLeaf;
        assertEquals(Optional.of("Some string after the double backslash"), otherLeafSchemaNode.getDescription());
        assertThat(otherLeafSchemaNode.getType(), instanceOf(Int32TypeDefinition.class));
    }
}
